package com.pisfly;

import com.pisfly.util.StreamProcessor;
import com.pisfly.web.WebConfig;
import org.apache.commons.cli.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.*;

/**
 * Created by fatihdonmez on 16/08/15
 */
public class EncodingApplication {

    public static void main(String[] args) {

        System.out.println("Use --help to see available options");

        Options options = new Options();

        Option help = Option.builder("h").longOpt("help").hasArg(false).desc("display usage").build();
        Option ascii = Option.builder("a").longOpt("ascii").hasArg(false).desc("base ascii, default Hex").build();
        Option server = Option.builder("s").longOpt("server").hasArg(false).desc("server mode").build();
        Option port = Option.builder("p").longOpt("port").numberOfArgs(1).desc("server port, default 8888").build();
        Option mode = Option.builder("m").longOpt("mode").numberOfArgs(1).desc("operation mode encode or decode").build();
        Option input = Option.builder("i").longOpt("input").numberOfArgs(2).valueSeparator(' ').desc("input and output file path to encode/decode").build();

        options.addOption(help);
        options.addOption(ascii);
        options.addOption(server);
        options.addOption(port);
        options.addOption(mode);
        options.addOption(input);

        CommandLineParser parser = new DefaultParser();

        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );
            HelpFormatter formatter = new HelpFormatter();

            if(line.hasOption("help")) {
                formatter.printHelp( "Encoder", options );
                return;
            }

            //Command line interface
            if(!line.hasOption("s")) {

                System.out.println("--> Command line mode started");

                boolean isAscii = false;
                String encodeMode = "";

                if(line.hasOption("a")) {
                    System.out.println("--> Ascii mode enabled");
                    isAscii = true;
                } else {
                    System.out.println("--> Default hex mode enabled");
                }

                if (line.hasOption("m")) {
                    encodeMode = line.getOptionValue("m");

                    if (!encodeMode.equals("encode") && !encodeMode.equals("decode")) {
                        new ParseException("Operation mode invalid. Use encode or decode");
                    }

                    System.out.println("--> Operation mode set to {" + encodeMode + "}");
                } else {
                    System.out.println("--> Default operation mode set to {decode}");
                }

                //Command line text interface
                if(!line.hasOption("i")) {
                    System.out.println("--> Default interactive shell enabled\n");
                    textInterface(isAscii,encodeMode.equals("encode") ? true : false);

                } else { // Command line file interface
                    System.out.println("--> File mode enabled\n");
                    fileInterface(line.getOptionValues("i")[0],line.getOptionValues("i")[1],isAscii,encodeMode.equals("encode") ? true : false);
                }
            } else { //web server interface
                System.out.println("--> Server mode enabling\n");
                startServer(line.getOptionValue("port","8888"));
            }

        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        }
    }

    private static void textInterface(boolean ascii, boolean isEncode) {

        BufferedInputStream inputStream = null;
        PrintWriter outputStream = null;

        try {

            System.out.print("Data to encode or decode:");
            inputStream = new BufferedInputStream(System.in);
            outputStream = new PrintWriter(System.out);

            StreamProcessor sp = new StreamProcessor(inputStream, outputStream, ascii, isEncode);
            sp.process2();

            outputStream.flush();
            sp.close();

        } catch (Exception e) {
            System.err.println("Error while processing data, invalid data stream - " + e.getMessage());
        } finally {

            try {
                inputStream.close();
                outputStream.close();
            } catch (Exception e) {
                System.err.println("Error while closing streams");
            }
        }

    }


    private static void fileInterface(String inputFilePath, String outputFilePath, boolean ascii, boolean isEncode) {

        BufferedInputStream inputStream = null;
        PrintWriter outputStream = null;

        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;

        try {

            File inputFile = new File(inputFilePath);
            fileInputStream = new FileInputStream(inputFile);

            File outputFile = new File(outputFilePath);
            fileOutputStream = new FileOutputStream(outputFile);

            inputStream = new BufferedInputStream(fileInputStream);
            outputStream = new PrintWriter(fileOutputStream);

            StreamProcessor sp = new StreamProcessor(inputStream, outputStream, ascii, isEncode);
            sp.process2();

            outputStream.flush();
            sp.close();

            System.out.println("Operation is completed successfully, see the path " + outputFilePath );

        } catch (FileNotFoundException e) {
            System.err.println("File not found - " + e.getMessage());
        } finally {

            try {
                inputStream.close();
                outputStream.close();
                fileInputStream.close();
                fileOutputStream.close();
            } catch (Exception e) {
                System.err.println("Error while closing streams - " + e.getMessage());
            }
        }

    }

    /**
     * Start embedded jetty server and configure SpringMVC
     * @param port
     */
    private static void startServer(String port) {

        try {
            AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
            applicationContext.register(WebConfig.class);

            ServletContextHandler context = new ServletContextHandler();
            context.setContextPath("/");
            context.setResourceBase(new ClassPathResource("webapp").getURI().toString());

            ServletHolder servletHolder = new ServletHolder(new DispatcherServlet(applicationContext));
            context.addServlet(servletHolder, "/*");

            final Server server = new Server(Integer.valueOf(port));

            server.setHandler(context);

            server.start();
            server.join();
        } catch (Exception e) {
            System.err.println("Web server start failed with " + e.getMessage());
        }
    }
}
