package com.pos.controller;

import com.pos.dto.SalesReportDto;
import com.pos.exception.ApiException;
import com.pos.model.data.SalesReportData;
import com.pos.model.form.SalesReportForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports/sales")
public class SalesReportController {

    @Autowired
    private SalesReportDto salesReportDto;

    @RequestMapping(method = RequestMethod.GET)
    public List<SalesReportData> get(@Valid SalesReportForm form) throws ApiException {
        return salesReportDto.getCheck(form);
    }
}
