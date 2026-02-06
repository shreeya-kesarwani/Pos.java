package com.pos.controller;

import com.pos.dto.DaySalesDto;
import com.pos.exception.ApiException;
import com.pos.model.data.DaySalesData;
import com.pos.model.form.DaySalesForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports/day-sales")
public class DaySalesController {

    @Autowired
    private DaySalesDto daySalesDto;

    @RequestMapping(method = RequestMethod.GET)
    public List<DaySalesData> get(DaySalesForm form) throws ApiException {
        return daySalesDto.get(form);
    }
}
