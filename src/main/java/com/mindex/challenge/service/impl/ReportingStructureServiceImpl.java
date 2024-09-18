package com.mindex.challenge.service.impl;

import java.util.*;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.service.ReportingStructureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportingStructureServiceImpl implements ReportingStructureService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportingStructureServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public ReportingStructure resolve(String id) {
        LOG.debug("Resolving for employee [{}]", id);

        ReportingStructure rs = new ReportingStructure();
		Employee seed = employeeRepository.findByEmployeeId(id);
		if (seed == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }
        rs.setEmployee(seed);

		//BFS to find our sub-direct reports
		Queue<String> queue = new LinkedList<String>();
		//We need to avoid cycles, in case this isn't necessarily a tree
		HashSet<String> seen = new HashSet<String>();
		queue.add(seed.getEmployeeId());
		while(!queue.isEmpty()){
			String curr = queue.poll();
			seen.add(curr);
			Employee currentEmployee = employeeRepository.findByEmployeeId(curr);
			
			if(currentEmployee.getDirectReports()!=null){
				for(Employee sub : currentEmployee.getDirectReports()){
					//When I made the given GET calls in postman, 
					//the direct report objects were all null but for the eID
					Employee tempEmp = employeeRepository.findByEmployeeId(sub.getEmployeeId());
					if(tempEmp==null){
						continue;
					}
					//We can't visit someone we've already visited
					//We also can't visit someone we've already planned to visit
					if(!seen.contains(tempEmp.getEmployeeId())&&
						!queue.contains(tempEmp.getEmployeeId())){
						queue.add(tempEmp.getEmployeeId());
					}
				}
			}
		}
		//we correctly mark the first element as seen, but in this case we don't want it, so subtract one
		//if we fail to find the first element, we throw an exception, per the pattern in employee GET
        rs.setNumberOfReports(seen.size()-1);

        return rs;
    }
}

