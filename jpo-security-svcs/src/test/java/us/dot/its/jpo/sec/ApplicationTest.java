package us.dot.its.jpo.sec;

import org.junit.Test;
import org.springframework.boot.SpringApplication;

import mockit.Capturing;
import mockit.Expectations;

public class ApplicationTest {

   @Capturing
   SpringApplication capturingSpringApplication;

   @Test
   public void test() {
      new Expectations() {
         {
            SpringApplication.run((Class<?>) any, (String[]) any);
            times = 1;
         }
      };
      Application.main(new String[] { "testArg" });
   }

}
