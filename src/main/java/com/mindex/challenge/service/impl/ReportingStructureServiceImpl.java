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
import java.util.UUID;

@Service
public class ReportingStructureServiceImpl implements ReportingStructureService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportingStructureServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public ReportingStructure resolve(String id) {
        LOG.debug("Resolving for employee [{}]", id);

        // employee.setEmployeeId(UUID.randomUUID().toString());
        // employeeRepository.insert(employee);
        ReportingStructure rs = new ReportingStructure();
	Employee seed = employeeRepository.findByEmployeeId(id);
	//Didn't read what to do if the id is invalid
	if (seed == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }
        rs.setEmployee(seed);

	//BFS to find our sub-direct reports
	Queue<Employee> queue = new LinkedList<Employee>();
	//We need to avoid cycles, in case this isn't necessarily a tree
	HashSet<Employee> seen = new HashSet<Employee>();
	queue.add(seed);
	while(!queue.isEmpty()){
		Employee curr = queue.poll();
		System.out.println(curr.getFirstName()+" "+curr.getLastName());
		seen.add(curr);
		
		if(curr.getDirectReports()!=null){
			System.out.println("dirs="+curr.getDirectReports().get(0).getEmployeeId());
			System.out.println("seen="+seen.size());
			for(Employee sub : curr.getDirectReports()){
				//we will need to do this to get their DRs
				//When I made the given GET calls in postman, the direct report objects were all null but for the eID
				Employee temp = employeeRepository.findByEmployeeId(sub.getEmployeeId());
				//We can't visit someone we've already visited
				//we also can't visit someone we've already planned to visit
				if(!seen.contains(temp)&&!queue.contains(temp)){
					queue.add(temp);
				}
			}
		}
	}
	//we correctly mark the first element as seen, but in this case we don't want it, so subtract one
	//there is no 0 case, as if we fail to find the first element, we throw an exception, per the pattern in employee GET
        rs.setNumberOfReports(seen.size()-1);

        return rs;
    }
}

