package application.employee;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;

@Entity
public class Employee {

  @Id
  @GeneratedValue
  private Long id;

  @NotNull
  @Column(length = 50)
  private String firstName;

  @NotNull
  @Column(length = 50)
  private String lastName;

  @ManyToMany
  private Set<Employee> managers;

  public Employee() {
  }

  public Employee(String firstName, String lastName, Employee... managers) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.managers = new HashSet<>(Arrays.asList(managers));
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Set<Employee> getManagers() {
    return managers;
  }

  public void setManagers(Set<Employee> managers) {
    this.managers = managers;
  }
}
