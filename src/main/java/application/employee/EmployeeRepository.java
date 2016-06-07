package application.employee;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource
public interface EmployeeRepository extends CrudRepository<Employee, Long> {

  @RestResource(path = "byFields")
  Set<Employee> findByFirstNameContainingAndLastNameContainingAllIgnoreCase(@Param("firstName") String firstName, @Param("lastName") String lastName);

  @RestResource(exported = false)
  @Override
  void delete(Long id);

  @RestResource(exported = false)
  @Override
  void delete(Employee entity);

  @RestResource(exported = false)
  @Override
  void delete(Iterable<? extends Employee> entities);

  @RestResource(exported = false)
  @Override
  void deleteAll();
}
