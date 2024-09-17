package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.EmployeeService;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class CompensationServiceImpl implements CompensationService {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private CompensationRepository compensationRepository;

    @Override
    public Compensation create(Compensation compensation) {
        LOG.debug("Creating compensation [{}]", compensation);
	if(compensation.getEmployee() == null){
		throw new RuntimeException("null employee!");
	}
        //assumption: the employee should already exist
	Employee employee = employeeRepository.findByEmployeeId(compensation.getEmployee().getEmployeeId());

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + employeeRepository.findByEmployeeId(compensation.getEmployee().getEmployeeId()));
        }


        compensationRepository.insert(compensation);

        return compensation;
    }

    @Override
    public Compensation read(String id) {
        LOG.debug("Reading compensation with id [{}]", id);
	Employee employee = employeeRepository.findByEmployeeId(id);
        if(employee == null){
		throw new RuntimeException("Invalid employeeId: " + id);
	}	
	Compensation compensation = new Compensation();
        compensation.setEmployee(employee);
        compensation.setSalary(compensationRepository.findByEmployee(employee).getSalary());
        compensation.setEffectiveDate(compensationRepository.findByEmployee(employee).getEffectiveDate());

        return compensation;
    }
}

