package com.goldin.echo;

import org.eclipse.jetty.http.MimeTypes.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;


@Controller
@EnableAutoConfiguration
public class EchoController {

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private HttpServletResponse response;


  @RequestMapping("/")
  @ResponseBody
  String home() {
    final StringBuilder result = new StringBuilder();
    response.setContentType( Type.TEXT_PLAIN_UTF_8.toString());

    for ( String header: Collections.list( request.getHeaderNames())) {
      List<String> values = Collections.list( request.getHeaders( header ));
      result.append( String.format( "%s:%s\n", header, values.isEmpty()   ? "" :
                                                       values.size() == 1 ? values.get( 0 ) :
                                                                            values ));
    }

    return result.toString();
  }

  public static void main( String[] args ) throws Exception {
    SpringApplication.run( EchoController.class, args );
  }
}
