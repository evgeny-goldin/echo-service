package com.goldin.echo;

import static org.springframework.util.MimeTypeUtils.TEXT_PLAIN_VALUE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;


@Controller
@EnableAutoConfiguration
public class EchoController {

  private final static Logger LOG = Logger.getLogger( EchoController.class.getName());

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private HttpServletResponse response;


  @RequestMapping( "**" )
  @ResponseBody
  String home() throws IOException, ServletException {
    response.setContentType( TEXT_PLAIN_VALUE );

    final StringBuilder dump   = new StringBuilder( RequestDumper.dump( request ));
    final String        gitSha = System.getenv( "GIT_SHA" );

    if ( gitSha != null ){
      dump.append( String.format( "\n[%s]", gitSha ));
    }

    final String response = dump.toString();
    LOG.info( response );
    return response;
  }


  public static void main( String[] args ) throws Exception {
    SpringApplication.run( EchoController.class, args );
  }
}
