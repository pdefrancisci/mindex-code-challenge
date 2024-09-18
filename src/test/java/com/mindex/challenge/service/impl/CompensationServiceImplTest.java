package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.ReportingStructureService;
import com.mindex.challenge.service.CompensationService;

import org.assertj.core.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {
    final int EXP_NUM_REPORTS = 4;

    private String employeeUrl;
    private String employeeIdUrl;

    private String compensationUrl;
    private String compensationIdUrl;

    private String reportingStructureIdUrl;

    @Autowired
    private ReportingStructureService reportingStructureService;

    @Autowired
    private CompensationService compensationService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        reportingStructureIdUrl = "http://localhost:" + port + "/reportingStructure/{id}";
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        compensationUrl = "http://localhost:" + port + "/compensation";
        compensationIdUrl = "http://localhost:" + port + "/compensation/{id}";
    }

    @Test
    public void testReportingStructure() {
        Employee lennon = new Employee();
        lennon.setFirstName("John");
        lennon.setLastName("Lennon");
        lennon.setDepartment("Engineering");
        lennon.setPosition("Developer");

        lennon = restTemplate.postForEntity(
            employeeUrl, lennon, Employee.class).getBody();

        //set comp
        Compensation lennonComp = new Compensation();
        lennonComp.setEmployee(lennon);
        lennonComp.setSalary(99);
        lennonComp.setEffectiveDate("1960s/1970s");
        
        lennonComp = restTemplate.postForEntity(
            compensationUrl, lennonComp, Compensation.class).getBody();

        //get comp, get employee
        Compensation recComp = restTemplate.getForEntity(
            compensationIdUrl, Compensation.class, lennon.getEmployeeId()).getBody();
        Employee recLennon = restTemplate.getForEntity(
            employeeIdUrl, Employee.class, lennon.getEmployeeId()).getBody();
        assertEquals(lennon.getEmployeeId(), recLennon.getEmployeeId());
        assertEmployeeEquivalence(lennon, recLennon);
        assertCompensationEquivalence(recComp,lennonComp);

        //update user    
        lennon.setLastName("Lennon-Ono");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        lennon =
            restTemplate.exchange(employeeIdUrl,
                    HttpMethod.PUT,
                    new HttpEntity<Employee>(lennon, headers),
                    Employee.class,
                    lennon.getEmployeeId()).getBody();

        //get comp, get employee
        recComp = restTemplate.getForEntity(
            compensationIdUrl, Compensation.class, lennon.getEmployeeId()).getBody();
        recLennon = restTemplate.getForEntity(
            employeeIdUrl, Employee.class, lennon.getEmployeeId()).getBody();
        assertEquals(lennon.getEmployeeId(), recLennon.getEmployeeId());
        assertEmployeeEquivalence(lennon, recLennon);
        assertEquals(recComp.getSalary(),lennonComp.getSalary());
        assertEquals(recComp.getEffectiveDate(),lennonComp.getEffectiveDate());
        //They should be different, the update to employee should propogate to our table
        assertNotEquals(recComp.getEmployee().getLastName(), lennonComp.getEmployee().getLastName());
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }

    private static void assertCompensationEquivalence(Compensation expected, Compensation actual) {
        assertEmployeeEquivalence(expected.getEmployee(), actual.getEmployee());
        assertEquals(expected.getSalary(), actual.getSalary());
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
    }
}
