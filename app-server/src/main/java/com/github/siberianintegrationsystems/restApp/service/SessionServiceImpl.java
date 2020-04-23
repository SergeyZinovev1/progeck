package com.github.siberianintegrationsystems.restApp.service;

import com.github.siberianintegrationsystems.restApp.controller.dto.sessiondto.SessionAnsweredQuestionDTO;
import com.github.siberianintegrationsystems.restApp.controller.dto.sessiondto.SessionQuestionAnswerDTO;
import com.github.siberianintegrationsystems.restApp.controller.dto.sessiondto.SessionRequestDTO;
import com.github.siberianintegrationsystems.restApp.data.AnswerRepository;
import com.github.siberianintegrationsystems.restApp.data.QuestionRepository;
import com.github.siberianintegrationsystems.restApp.data.SelectedAnswerRepository;
import com.github.siberianintegrationsystems.restApp.data.SessionRepository;
import com.github.siberianintegrationsystems.restApp.entity.Answer;
import com.github.siberianintegrationsystems.restApp.entity.Question;
import com.github.siberianintegrationsystems.restApp.entity.SelectedAnswer;
import com.github.siberianintegrationsystems.restApp.entity.Session;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional
public class SessionServiceImpl implements SessionService{

    private SessionRepository sessionRepository;
    private QuestionRepository questionRepository;
    private AnswerRepository answerRepository;
    private SelectedAnswerRepository selectedAnswerRepository;

    public SessionServiceImpl(SessionRepository sessionRepository,
                              QuestionRepository questionRepository,
                              AnswerRepository answerRepository,
                              SelectedAnswerRepository selectedAnswerRepository) {
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.selectedAnswerRepository = selectedAnswerRepository;
    }

    public double checkQuestion(SessionAnsweredQuestionDTO sessionAnsweredQuestionDTO) {
        Question question = questionRepository
                .findById(Long.parseLong(sessionAnsweredQuestionDTO.id))
                .orElseThrow(() ->
                        new RuntimeException(String.format("Отсутствует вопрос с id%s",
                                sessionAnsweredQuestionDTO.id)));

        double n = sessionAnsweredQuestionDTO.answersList.size();
        double m = answerRepository.findByQuestion(question)
                .stream()
                .filter(Answer::getCorrect)
                .count();
        if (m == 0) {
            throw new RuntimeException(
                    String.format("Нет ответов для вопроса с id: %d",
                            question.getId()));
        } else if (n == m) {
            throw new RuntimeException(
                    String.format("Ответы на вопрос с id: %d верные",
                            question.getId()));
        }
        double k = 0;
        double w = 0;

        List<SessionQuestionAnswerDTO> selectedAnswers = sessionAnsweredQuestionDTO.answersList;
        for (SessionQuestionAnswerDTO selectedСorrectAnswer : selectedAnswers) {
            Answer savedСorrectAnswer = answerRepository.findById(Long.parseLong(selectedСorrectAnswer.id))
                    .orElseThrow(() -> new RuntimeException(
                            String.format("Нет ответ с id: %s", selectedСorrectAnswer.id)));
            if (selectedСorrectAnswer.isSelected) {
                if (savedСorrectAnswer.getCorrect()) {
                    k++;
                } else {
                    w++;
                }
            }

        }
        return Math.max(0, k / m - w / (n - m));
    }


    @Override
    public String validateSession(SessionRequestDTO sessionRequestDTO) {
        Double result = sessionRequestDTO.questionsList.stream()
                .map(this::checkQuestion).reduce(0.0, Double::sum);

        result = result / sessionRequestDTO.questionsList.size() * 100.0;


        Session session = new Session(sessionRequestDTO.name, result);
        sessionRepository.save(session);


        List<Answer> answers = new ArrayList<>();
        for (SessionAnsweredQuestionDTO qestion : sessionRequestDTO.questionsList) {
            answers.addAll(qestion.answersList.stream()
                    .filter(sessionAnsweredQuestionDTO -> sessionAnsweredQuestionDTO.isSelected)
                    .map(sessionAnsweredQuestionDTO -> answerRepository.findById(Long.parseLong(sessionAnsweredQuestionDTO.id))
                            .orElseThrow(RuntimeException::new))
                    .collect(Collectors.toList()));
        }

        for (Answer a : answers) {
            selectedAnswerRepository.save(new SelectedAnswer(a, session));
        }
        return String.format("%.2f", result);

    }

}
