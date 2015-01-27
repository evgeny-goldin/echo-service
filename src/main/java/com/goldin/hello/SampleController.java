package com.goldin.hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@EnableAutoConfiguration
public class SampleController {

  @RequestMapping("/")
  @ResponseBody
  String home() {
    return new SimpleDateFormat().format( new Date());
  }

  public static void main( String[] args ) throws Exception {
    SpringApplication.run(SampleController.class, args);
  }
}
