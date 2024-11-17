package com.example.todo.test;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        StandardUserTests.class,
        CompanyAdminTests.class,
        SuperUserTests.class,
        GeneralTests.class
})
public class TestSuite {
    // Bu sınıf sadece diğer test sınıflarını çalıştırır.
    // Ek bir kod yazmaya gerek yoktur.
}
