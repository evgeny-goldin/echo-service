package com.goldin.echo;

import static org.springframework.util.MimeTypeUtils.MULTIPART_FORM_DATA_VALUE;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;


class RequestDumper {

  private static final String SEPARATOR_LINE = "------------------------------";


  private RequestDumper(){}


  static String dump( final HttpServletRequest request )
    throws IOException, ServletException {

    final StringBuilder result = new StringBuilder( title( "Request" )).
      append( pair( "Method",          request.getMethod().toUpperCase( Locale.ENGLISH ))).
      append( pair( "Protocol",        request.getProtocol())).
      append( pair( "Scheme",          request.getScheme())).
      append( pair( "Encoding",        request.getCharacterEncoding())).
      append( pair( "Request URI",     request.getRequestURI())).
      append( pair( "Request URL",     request.getRequestURL().toString())).
      append( pair( "Content Type",    request.getContentType())).
      append( pair( "Query String",    request.getQueryString())).
      append( pair( "Auth Type",       request.getAuthType())).
      append( pair( "Path Info",       request.getPathInfo())).
      append( pair( "Path Translated", request.getPathTranslated())).
      append( pair( "Remote User",     request.getRemoteUser())).
      append( pair( "Remote Address",  request.getRemoteAddr())).
      append( pair( "Remote Host",     request.getRemoteHost())).
      append( pair( "Remote Port",     string( request.getRemotePort())));

    result.append( string( "Headers", iterable( request.getHeaderNames()), new Function<String, String>(){
      @Override public String apply ( String header ) {
        return pair( header, string( iterable( request.getHeaders( header ))));
      }}));

    result.append( string( "Locales", iterable( request.getLocales()), new Function<Locale, String>(){
      @Override public String apply ( Locale locale ){
        return line( locale.toLanguageTag());
      }}));

    if (( request.getContentType() != null ) && request.getContentType().startsWith( MULTIPART_FORM_DATA_VALUE )) {
      result.append( formParts( request.getParts()));
    }

    result.append( string( "Parameters", iterable( request.getParameterNames()), new Function<String, String>(){
      @Override public String apply ( String parameter ) {
        return pair( parameter, string( Arrays.asList( request.getParameterValues( parameter ))));
      }}));

    result.append( title( "Body" ));
    result.append( line( string( request.getInputStream())));

    return result.toString();
  }


  @SuppressWarnings({ "StringBufferReplaceableByString", "InnerClassTooDeeplyNested" })
  private static String formParts( Iterable<Part> parts ) {
    return string( "Form Parts", parts, new Function<Part, String>(){
      @Override public String apply ( final Part part ){
        return new StringBuilder( title( part.getName())).
          append( pair( "Content Type", part.getContentType())).
          append( pair( "File Name",    part.getSubmittedFileName())).
          append( pair( "Size",         string( part.getSize()))).
          append( string( "Headers", part.getHeaderNames(), new Function<String, String>(){
            @Override public String apply ( String header ){
              return pair( header, string( part.getHeaders( header )));
            }})).toString();
      }});
  }


  private static String pair( String key, String value ) {
    return (( value == null ) ? "" : line( String.format( "%s: %s", key, value )));
  }


  private static String title( String title ) {
    return String.format( "%s%s%s", line( SEPARATOR_LINE ), line( title ), line( SEPARATOR_LINE ));
  }


  private static String line( String line ) {
    return line + "\n";
  }


  private static <T> String string( String title, Iterable<T> collection, Function<T, String> f ) {
    StringBuilder result = new StringBuilder( title( title ));
    for( T t : collection ) { result.append( f.apply( t )); }
    return result.toString();
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


  private static String string( long number ) {
    return String.valueOf( number );
  }


  @SuppressWarnings( "MethodOnlyUsedFromInnerClass" )
  private static <T> Iterable<T> iterable( Enumeration<T> e ) {
    return Collections.list( e );
  }
}
