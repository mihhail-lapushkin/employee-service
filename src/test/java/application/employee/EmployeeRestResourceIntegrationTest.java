package application.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JsonContentAssert;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.DefaultUriTemplateHandler;

import com.jayway.jsonpath.JsonPath;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class EmployeeRestResourceIntegrationTest {

  private static final Logger LOG = LoggerFactory.getLogger(EmployeeRestResourceIntegrationTest.class);

  TestRestTemplate prefixedTestRestTemplate = new TestRestTemplate();
  TestRestTemplate unprefixedTestRestTemplate = new TestRestTemplate();

  @Autowired
  EmployeeRepository employeeRepository;

  @Autowired
  EntityManager entityManager;

  @LocalServerPort
  int localServerPort;

  String getRepositoryPath() {
    return String.format("http://localhost:%d/employees", localServerPort);
  }

  String getRepositoryPrefix() {
    return String.format("%s/", getRepositoryPath());
  }

  String buildHrefFromId(Long id) {
    return String.format("%s%d", getRepositoryPrefix(), id);
  }

  @Before
  public void setUp() {
    prefixedTestRestTemplate.setUriTemplateHandler(new DefaultUriTemplateHandler() {
      @Override
      public String getBaseUrl() {
        return getRepositoryPath();
      }
    });

    employeeRepository.deleteAll();
  }

  @Test
  public void getAllShouldReturnAllEmployeesFromDatabase() {
    employeeRepository.save(new Employee("john", "smith"));
    employeeRepository.save(new Employee("jim", "beam"));

    assertJson(get("/"))
      .extractingJsonPathArrayValue("_embedded.employees")
      .extracting("firstName", "lastName")
      .containsOnly(
        tuple("john", "smith"),
        tuple("jim", "beam")
      );
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getOneShouldReturnExisitngEmployeesAndTheirManagersFromDatabase() {
    Employee boss = employeeRepository.save(new Employee("john", "boss"));
    Employee employee = employeeRepository.save(new Employee("jim", "employee", boss));

    String bossJson = get("/{id}", boss.getId());
    String employeeJson = get("/{id}", employee.getId());

    assertJson(bossJson)
      .extractingJsonPathMapValue("@")
      .contains(
        entry("firstName", "john"),
        entry("lastName", "boss")
      );

    assertJson(getByUrlAtJsonPath("_links.managers.href", bossJson))
      .extractingJsonPathArrayValue("_embedded.employees")
      .isEmpty();

    assertJson(employeeJson)
      .extractingJsonPathMapValue("@")
      .contains(
        entry("firstName", "jim"),
        entry("lastName", "employee")
      );

    assertJson(getByUrlAtJsonPath("_links.managers.href", employeeJson))
      .extractingJsonPathArrayValue("_embedded.employees")
      .extracting("firstName", "lastName")
      .containsOnly(
        tuple("john", "boss")
      );
  }

  @Test
  public void getOneShouldReturnNothingForNonExistingEmployee() {
    assertThat(get("/{id}", 1)).isNullOrEmpty();
  }

  @Test
  public void searchShouldLookupEmployeesUsingContainsIgnoringCasePattern() {
    employeeRepository.save(new Employee("John", "Smith"));
    employeeRepository.save(new Employee("Jim", "Beam"));
    employeeRepository.save(new Employee("Ted", "Baker"));
    employeeRepository.save(new Employee("Adam", "Sandler"));

    assertJson(get("/search/byFields?firstName={1}&lastName={2}", "j", ""))
      .extractingJsonPathArrayValue("_embedded.employees")
      .extracting("firstName", "lastName")
      .containsOnly(
        tuple("John", "Smith"),
        tuple("Jim", "Beam")
      );

    assertJson(get("/search/byFields?firstName={1}&lastName={2}", "DA", "ANDL"))
      .extractingJsonPathArrayValue("_embedded.employees")
      .extracting("firstName", "lastName")
      .containsOnly(
        tuple("Adam", "Sandler")
      );
  }

  @Test
  public void searchShouldReturnEmptyListIfNoMatchesFound() {
    employeeRepository.save(new Employee("John", "Smith"));
    employeeRepository.save(new Employee("Jim", "Beam"));
    employeeRepository.save(new Employee("Ted", "Baker"));
    employeeRepository.save(new Employee("Adam", "Sandler"));

    assertJson(get("/search/byFields?firstName={1}&lastName={2}", "NON", "EXISTING"))
      .extractingJsonPathArrayValue("_embedded.employees")
      .isEmpty();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void createShouldAddNewEmployeeAndAssignManagers() {
    Employee boss = employeeRepository.save(new Employee("john", "boss"));

    String employeeCreationJsonRequest = prepareJson("'firstName':'ted', 'lastName':'employee', 'managers':[ '%s' ]", buildHrefFromId(boss.getId()));
    String employeeCreationJsonResponse = post(employeeCreationJsonRequest, "/");
    String createdEmployeeJson = getByUrlAtJsonPath("_links.self.href", employeeCreationJsonResponse);

    assertJson(createdEmployeeJson)
      .extractingJsonPathMapValue("@")
      .contains(
        entry("firstName", "ted"),
        entry("lastName", "employee")
      );

    assertJson(getByUrlAtJsonPath("_links.managers.href", createdEmployeeJson))
      .extractingJsonPathArrayValue("_embedded.employees")
      .extracting("firstName", "lastName")
      .containsOnly(
        tuple("john", "boss")
      );
  }

  @SuppressWarnings("unchecked")
  @Test
  public void updateShouldModifyEmployee() {
    Employee boss1 = employeeRepository.save(new Employee("john", "boss"));
    Employee boss2 = employeeRepository.save(new Employee("gary", "boss"));
    Employee employee = employeeRepository.save(new Employee("jim", "employee", boss1));

    String employeeUpdateJsonRequest = prepareJson("'firstName':'bill', 'lastName':'gates', 'managers':[ '%s', '%s' ]", buildHrefFromId(boss1.getId()), buildHrefFromId(boss2.getId()));
    put(employeeUpdateJsonRequest, "/{id}", employee.getId());
    String updatedEmployeeJson = get("/{id}", employee.getId());

    assertJson(updatedEmployeeJson)
      .extractingJsonPathMapValue("@")
      .contains(
        entry("firstName", "bill"),
        entry("lastName", "gates")
      );

    assertJson(getByUrlAtJsonPath("_links.managers.href", updatedEmployeeJson))
      .extractingJsonPathArrayValue("_embedded.employees")
      .extracting("firstName", "lastName")
      .containsOnly(
        tuple("john", "boss"),
        tuple("gary", "boss")
      );
  }

  @Test
  public void deleteShouldNotBeAllowed() {
    Employee employee = employeeRepository.save(new Employee("jim", "beam"));

    assertThat(deleteReturningResponseStatus("/{id}", employee.getId())).isEqualTo(METHOD_NOT_ALLOWED);
    assertThat(get("/{id}", employee.getId())).isNotEmpty();
  }

  String getByUrlAtJsonPath(String path, String json) {
    return httpRequest(unprefixedTestRestTemplate, GET, null, getJsonValueByPath(path, json)).getBody();
  }

  String get(String url, Object... urlVariables) {
    return httpRequest(prefixedTestRestTemplate, GET, null, url, urlVariables).getBody();
  }

  String post(String json, String url, Object... urlVariables) {
    return httpRequest(prefixedTestRestTemplate, POST, json, url, urlVariables).getBody();
  }

  String put(String json, String url, Object... urlVariables) {
    return httpRequest(prefixedTestRestTemplate, PUT, json, url, urlVariables).getBody();
  }

  HttpStatus deleteReturningResponseStatus(String url, Object... urlVariables) {
    return httpRequest(prefixedTestRestTemplate, DELETE, null, url, urlVariables).getStatusCode();
  }

  ResponseEntity<String> httpRequest(TestRestTemplate restTemplate, HttpMethod method, String json, String url, Object... urlVariables) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    ResponseEntity<String> response = restTemplate.exchange(url, method, new HttpEntity<>(json, headers), String.class, urlVariables);

    LOG.info("Request URL: {}", restTemplate.getRestTemplate().getUriTemplateHandler().expand(url, urlVariables));
    LOG.info("Request Method: {}", method);
    LOG.info("Request Body: {}", json);
    LOG.info("Response Status: {}", response.getStatusCode().getReasonPhrase());
    LOG.info("Response Body: {}", response.getBody());

    return response;
  }

  Long getIdFromHrefJsonPath(String path, String json) {
    return Long.valueOf(getJsonValueByPath(path, json).replace(getRepositoryPrefix(), ""));
  }

  String getJsonValueByPath(String path, String json) {
    return JsonPath.compile(path).read(json);
  }

  String prepareJson(String jsonTemplate, Object... args) {
    return String.format("{" + jsonTemplate.replaceAll("'", "\"") + "}", args);
  }

  JsonContentAssert assertJson(String json) {
    return new JsonContentAssert(EmployeeRestResourceIntegrationTest.class, json);
  }
}
