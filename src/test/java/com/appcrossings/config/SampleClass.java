package com.appcrossings.config;

import org.springframework.beans.factory.annotation.Value;

public class SampleClass {

  @Value("${property.1.name}")
  public String someValue1;

  @Value("${property.2.name}")
  public String someValue2;

  @Value("${property.4.name}")
  public String someValue4;


  public String bonus1;

  public String someOtherValue;

  public String getSomeValue4() {
    return someValue4;
  }

  public String getBonus1() {
    return bonus1;
  }

  @Value("${bonus.1.property:none}")
  public void setBonus1(String bonus1) {
    this.bonus1 = bonus1;
  }

  public void setSomeValue4(String someValue4) {
    this.someValue4 = someValue4;
  }

  public String getSomeOtherValue() {
    return someOtherValue;
  }


  public String getSomeValue1() {
    return someValue1;
  }


  public void setSomeValue1(String someValue1) {
    this.someValue1 = someValue1;
  }


  public String getSomeValue2() {
    return someValue2;
  }


  public void setSomeValue2(String someValue2) {
    this.someValue2 = someValue2;
  }


  public void setSomeOtherValue(String someOtherValue) {
    this.someOtherValue = someOtherValue;
  }


}
