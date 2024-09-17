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
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private CompensationRepository compensationRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Reading employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);
	//without this, save() was introducing a duplicate entry and I couldn't test update. No clue why, running ubuntu.
	LOG.debug("fields...",employee.getLastName());
	if(employee.getLastName()==null){
		LOG.debug("not true!!!!!!");
	}
	LOG.debug("getting old empployee, fetchild old comp, making new comp and saving both new objects...");
	Employee oldEmployee = employeeRepository.findByEmployeeId(employee.getEmployeeId());
	Compensation comp = compensationRepository.findByEmployee(oldEmployee);
	if(comp!=null){
		comp.setEmployee(employee);
		compensationRepository.save(comp);
	}
	//employeeRepository.delete(employee);
        return employeeRepository.save(employee);
    }
}
