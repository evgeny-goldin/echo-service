package com.goldin.echo;

import static org.springframework.util.MimeTypeUtils.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.util.MimeTypeUtils.TEXT_PLAIN_VALUE;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;


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
  String home() throws IOException, ServletException {
    response.setContentType( TEXT_PLAIN_VALUE );

    final StringBuilder result = new StringBuilder();

    result.append( title( "Request Information" ));

    result.append( pair( "Method",          request.getMethod().toUpperCase( Locale.ENGLISH ))).
           append( pair( "Protocol",        request.getProtocol())).
           append( pair( "Scheme",          request.getScheme())).
           append( pair( "Encoding",        request.getCharacterEncoding())).
           append( pair( "Request URI",     request.getRequestURI())).
           append( pair( "Request URL",     request.getRequestURL().toString())).
           append( pair( "Content Type",    request.getContentType())).
           append( pair( "Content Length",  string( request.getContentLengthLong()))).
           append( pair( "Context Path",    request.getContextPath())).
           append( pair( "Query String",    request.getQueryString())).
           append( pair( "Auth Type",       request.getAuthType())).
           append( pair( "Path Info",       request.getPathInfo())).
           append( pair( "Path Translated", request.getPathTranslated())).
           append( pair( "Remote User",     request.getRemoteUser())).
           append( pair( "Remote Address",  request.getRemoteAddr())).
           append( pair( "Remote Host",     request.getRemoteHost())).
           append( pair( "Remote Port",     string( request.getRemotePort())));

    if ( MULTIPART_FORM_DATA_VALUE.equals( request.getContentType())) {
      result.append( parts( request.getParts()));
    }

    result.append( collection( "Attributes", request.getAttributeNames(), new Function<String, String>(){
      @Override public String apply ( String attributeName ) {
        return pair( attributeName, request.getAttribute( attributeName ).toString());
      }}));

    result.append( collection( "Locales", request.getLocales(), new Function<Locale, String>(){
      @Override public String apply ( Locale locale ){
        return line( locale.toLanguageTag());
      }}));

    result.append( collection( "HTTP Headers", request.getHeaderNames(), new Function<String, String>(){
      @Override public String apply ( String header ) {
        return pair( header, string( iterable( request.getHeaders( header ))));
      }}));

    result.append( title( "Body" ));
    result.append( line( string( request.getInputStream())));

    return result.toString();
  }


  private static String parts( Iterable<Part> parts ) {
    return collection( "Form Parts", parts, new Function<Part, String>()
    {
      @Override
      public String apply ( final Part part )
      {
        StringBuilder result = new StringBuilder( title( part.getName()));
        //noinspection InnerClassTooDeeplyNested
        result.append( pair( "Content Type", part.getContentType())).
               append( pair( "File Name",    part.getSubmittedFileName())).
               append( pair( "Size",         string( part.getSize()))).
               append( collection( "Headers", part.getHeaderNames(), new Function<String, String>()
               {
                 @Override
                 public String apply ( String header )
                 {
                   return pair( header, string( part.getHeaders( header )));
                 }
               } ));

        try { result.append( pair( "Body", string( part.getInputStream())));}
        catch ( IOException ignored ){}

        return result.toString();
      }
    });
  }

  private static <T> String collection( String title, Enumeration<T> collection, Function<T, String> f ) {
    return collection( title, Collections.list( collection ), f );
  }


  private static <T> String collection( String title, Iterable<T> collection, Function<T, String> f ) {
    StringBuilder result = new StringBuilder( title( title ));
    for( T t : collection ) { result.append( f.apply( t )); }
    return result.toString();
  }


  private static String pair( String key, String value ) {
    return (( value == null ) ? "" : line( String.format( "%s:%s", key, value )));
  }


  private static String title( String title ) {
    return String.format( "%s%s%s", line( SEPARATOR_LINE ), line( title ), line( SEPARATOR_LINE ));
  }


  private static String string( InputStream is ) {
    try { return new String( ByteStreams.toByteArray( is ), Charset.forName( "UTF-8" )); }
    catch ( IOException e ) { return e.toString(); }
  }


  private static String string( Iterable<String> i ) {
    final List<String> l = Lists.newArrayList( i );
    return l.isEmpty()   ? "" :
           l.size() == 1 ? l.get( 0 ) :
                           l.toString();
  }

  private static String line( String line ) {
    return line + "\n";
  }


  private static String string( long number ) {
    return String.valueOf( number );
  }


  @SuppressWarnings( "MethodOnlyUsedFromInnerClass" )
  private static <T> Iterable<T> iterable( Enumeration<T> e ) {
    return Collections.list( e );
  }


  public static void main( String[] args ) throws Exception {
    SpringApplication.run( EchoController.class, args );
  }
}
