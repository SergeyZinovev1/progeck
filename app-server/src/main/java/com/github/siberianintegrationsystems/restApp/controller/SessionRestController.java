package com.github.siberianintegrationsystems.restApp.controller;

import com.github.siberianintegrationsystems.restApp.controller.dto.QuestionsItemDTO;
import com.github.siberianintegrationsystems.restApp.controller.dto.sessiondto.SessionRequestDTO;
import com.github.siberianintegrationsystems.restApp.data.AnswerRepository;
import com.github.siberianintegrationsystems.restApp.data.QuestionRepository;
import com.github.siberianintegrationsystems.restApp.service.QuestionService;
import com.github.siberianintegrationsystems.restApp.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("api")
public class SessionRestController {


    public SessionRestController(,
            QuestionService questionService) {
        this.sessionService = sessionService;
        this.questionService = questionService;
    }
    private SessionService sessionService;
    private QuestionService questionService;


    @GetMapping("session/questions-new")
    public List<QuestionsItemDTO> getQuestionsForSession(){
        return questionService.getQuestionsForSession();
    }
     @PostMapping("session")
    String saveSession(@RequestBody SessionRequestDTO sessionRequestDTO){
        return SessionService.validateSession(sessionRequestDTO);
     }









}
