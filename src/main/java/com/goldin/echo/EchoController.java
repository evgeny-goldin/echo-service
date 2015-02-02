package com.goldin.echo;

import com.google.common.io.ByteStreams;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


@Controller
@EnableAutoConfiguration
public class EchoController {

  private static final String SEPARATOR_LINE = "------------------------------";

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private HttpServletResponse response;


  @RequestMapping("/")
  @ResponseBody
  String home() throws IOException {
    response.setContentType( Type.TEXT_PLAIN_UTF_8.toString());

    final StringBuilder result = new StringBuilder();

    result.append( separator( "Request Information" ));

    result.append( pair( "Method",    request.getMethod().toUpperCase( Locale.ENGLISH ))).
           append( pair( "Auth Type", request.getAuthType()));

    result.append( separator( "HTTP Headers" ));

    for ( String header: Collections.list( request.getHeaderNames())) {
      List<String> values = Collections.list( request.getHeaders( header ));
      result.append( pair( header, values.isEmpty()   ? "" :
                                   values.size() == 1 ? values.get( 0 ) :
                                                        values.toString()));
    }

    result.append( separator( "Request Body" ));
    result.append( new String( ByteStreams.toByteArray( request.getInputStream()), Charset.forName( "UTF-8" )));

    return result.toString();
  }


  private static String pair( String key, String value ) {
    return (( value == null ) ? "" : String.format( "%s:%s\n", key, value ));
  }


  private static String separator( String title ) {
    return String.format( "%s\n%s\n%s\n", SEPARATOR_LINE, title, SEPARATOR_LINE );
  }


  public static void main( String[] args ) throws Exception {
    SpringApplication.run( EchoController.class, args );
  }
}
