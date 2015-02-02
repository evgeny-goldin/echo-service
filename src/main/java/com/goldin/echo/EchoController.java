package com.goldin.echo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@EnableAutoConfiguration
public class EchoController {

  @RequestMapping("/")
  @ResponseBody
  String home() {
    return "EEE";
  }

  public static void main( String[] args ) throws Exception {
    SpringApplication.run( EchoController.class, args );
  }
}
