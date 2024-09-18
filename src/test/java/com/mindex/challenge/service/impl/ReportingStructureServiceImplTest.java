package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;

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


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportingStructureServiceImplTest {
    final int EXP_NUM_REPORTS = 4;

    private String employeeUrl;
    private String employeeIdUrl;

    private String reportingStructureIdUrl;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        reportingStructureIdUrl = "http://localhost:" + port + "/reportingStructure/{id}";
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
    }

    @Test
    public void testReportingStructure() {
        //Create test data
        Employee lennon = new Employee();
        lennon.setFirstName("John");
        lennon.setLastName("Lennon");
        lennon.setDepartment("Engineering");
        lennon.setPosition("Developer");
        lennon = restTemplate.postForEntity(
            employeeUrl, lennon, Employee.class).getBody();
        
        Employee mccartney = new Employee();
        mccartney.setFirstName("paul");
        mccartney.setLastName("mccartney");
        mccartney.setDepartment("Engineering");
        mccartney.setPosition("Developer");
        mccartney = restTemplate.postForEntity(
            employeeUrl, mccartney, Employee.class).getBody();
        
        Employee ringo = new Employee();
        ringo.setFirstName("ringo");
        ringo.setLastName("starr");
        ringo.setDepartment("Engineering");
        ringo.setPosition("Developer");
        ringo = restTemplate.postForEntity(
            employeeUrl, ringo, Employee.class).getBody();

        Employee george = new Employee();
        george.setFirstName("george");
        george.setLastName("harrison");
        george.setDepartment("Engineering");
        george.setPosition("Developer");
        george = restTemplate.postForEntity(
            employeeUrl, george, Employee.class).getBody();

        Employee pete = new Employee();
        pete.setFirstName("pete");
        pete.setLastName("best");
        pete.setDepartment("Engineering");
        pete.setPosition("Developer");
        pete = restTemplate.postForEntity(
            employeeUrl, pete, Employee.class).getBody();

        //I'm populating the direct reports with updates
        List<Employee> georgeReps = new ArrayList<Employee>();
        georgeReps.add(pete);
        george.setDirectReports(georgeReps);

        List<Employee> johnReps = new ArrayList<Employee>();
        johnReps.add(mccartney);
        johnReps.add(ringo);
        johnReps.add(george);
        lennon.setDirectReports(johnReps);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //said updates
        lennon =
            restTemplate.exchange(employeeIdUrl,
                    HttpMethod.PUT,
                    new HttpEntity<Employee>(lennon, headers),
                    Employee.class,
                    lennon.getEmployeeId()).getBody();

        george.setDirectReports(georgeReps);
        george =
            restTemplate.exchange(employeeIdUrl,
                    HttpMethod.PUT,
                    new HttpEntity<Employee>(george, headers),
                    Employee.class,
                    george.getEmployeeId()).getBody();


        //John should find his 3 direct reports, plus george's one
        ReportingStructure rs = restTemplate.getForEntity(reportingStructureIdUrl, ReportingStructure.class, lennon.getEmployeeId()).getBody();
        assertEquals(rs.getNumberOfReports(), EXP_NUM_REPORTS);

        //should not break when a cycle is introduced
        //So pete, a child in the tree/graph of John, now points to him
        List<Employee> peteReps = new ArrayList<Employee>();
        peteReps.add(lennon);
        pete.setDirectReports(peteReps);
        pete =
            restTemplate.exchange(employeeIdUrl,
                    HttpMethod.PUT,
                    new HttpEntity<Employee>(pete, headers),
                    Employee.class,
                    pete.getEmployeeId()).getBody();

        rs = restTemplate.getForEntity(reportingStructureIdUrl, ReportingStructure.class, lennon.getEmployeeId()).getBody();
        assertEquals(rs.getNumberOfReports(), EXP_NUM_REPORTS);
    }
}
