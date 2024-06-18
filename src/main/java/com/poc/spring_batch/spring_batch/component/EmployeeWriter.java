package com.poc.spring_batch.spring_batch.component;

import com.poc.spring_batch.spring_batch.entity.Employee;
import com.poc.spring_batch.spring_batch.repository.EmployeeRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmployeeWriter implements ItemWriter<Employee>{

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public void write(Chunk<? extends Employee> chunk) throws Exception {
        System.out.println("Thread Name : " + Thread.currentThread().getName());
        employeeRepository.saveAll(chunk.getItems());
    }
}
