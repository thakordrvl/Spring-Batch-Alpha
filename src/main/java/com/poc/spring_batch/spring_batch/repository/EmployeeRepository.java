package com.poc.spring_batch.spring_batch.repository;
import com.poc.spring_batch.spring_batch.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
}
