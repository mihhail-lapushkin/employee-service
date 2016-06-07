package application.employee;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("generate-sample-data")
@Component
public class EmployeeSampleDataGenerator implements CommandLineRunner {

  private final EmployeeRepository employeeRepository;

  public EmployeeSampleDataGenerator(EmployeeRepository employeeRepository) {
    this.employeeRepository = employeeRepository;
  }

  @SuppressWarnings("unused")
  @Override
  public void run(String... args) throws Exception {
    Employee ceo = employeeRepository.save(new Employee("John", "CEO"));
    Employee bm1 = employeeRepository.save(new Employee("Bill", "BM"));
    Employee bm2 = employeeRepository.save(new Employee("Jim", "BM"));
    Employee bm3 = employeeRepository.save(new Employee("Mike", "BM"));

    Employee svp1 = employeeRepository.save(new Employee("Hannah", "SVP", ceo, bm2));
    Employee svp2 = employeeRepository.save(new Employee("Carol", "SVP", ceo));
    Employee svp3 = employeeRepository.save(new Employee("Eric", "SVP", ceo, bm2));
    Employee svp4 = employeeRepository.save(new Employee("Gunther", "SVP", ceo));
    Employee svp5 = employeeRepository.save(new Employee("Ugene", "SVP", ceo, bm3));

    Employee vp1 = employeeRepository.save(new Employee("Ted", "VP", svp1));
    Employee vp2 = employeeRepository.save(new Employee("Samuel", "VP", svp1));
    Employee vp3 = employeeRepository.save(new Employee("Nathan", "VP", svp2));
    Employee vp4 = employeeRepository.save(new Employee("Larry", "VP", svp2, svp3));
    Employee vp5 = employeeRepository.save(new Employee("Marry", "VP", svp4));
    Employee vp6 = employeeRepository.save(new Employee("Yankee", "VP", svp4));
    Employee vp7 = employeeRepository.save(new Employee("Ken", "VP", svp4));

    Employee tm1 = employeeRepository.save(new Employee("Top", "Manager 1", vp7));
    Employee tm2 = employeeRepository.save(new Employee("Top", "Manager 2", vp7, vp3));
    Employee tm3 = employeeRepository.save(new Employee("Top", "Manager 3", vp7));
    Employee tm4 = employeeRepository.save(new Employee("Top", "Manager 4", vp7));
    Employee tm5 = employeeRepository.save(new Employee("Top", "Manager 5", vp7));
    Employee tm6 = employeeRepository.save(new Employee("Top", "Manager 6", vp7, vp2));
    Employee tm7 = employeeRepository.save(new Employee("Top", "Manager 7", vp7));
  }
}
