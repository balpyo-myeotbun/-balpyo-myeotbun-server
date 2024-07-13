package site.balpyo.common.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import site.balpyo.common.ErrorLogRepository;
import site.balpyo.common.entity.ErrorLogEntity;

import java.util.List;

@Controller
public class ErrorLogController {

    @Autowired
    private ErrorLogRepository errorLogRepository;

    @GetMapping("/error-logs")
    public String getErrorLogs(Model model) {
        List<ErrorLogEntity> errorLogs = errorLogRepository.findTop100ByOrderByCreatedAtDesc();
        model.addAttribute("errorLogs", errorLogs);
        return "error-logs";
    }
}
