//package com.pos.service;
//
//import com.pos.dao.InvoiceDao;
//import com.pos.pojo.InvoicePojo;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import jakarta.transaction.Transactional;
//
//@Service
//public class InvoiceService {
//    @Autowired
//    private InvoiceDao dao;
//
//    @Transactional
//    public void add(InvoicePojo pojo) {
//        dao.insert(pojo);
//    }
//
//    @Transactional
//    public InvoicePojo getByOrderId(Integer orderId) {
//        return dao.selectByOrderId(orderId);
//    }
//}