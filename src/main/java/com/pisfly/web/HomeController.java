package com.pisfly.web;

import com.pisfly.util.StreamProcessor;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Created by FatihDonmez on 17/08/15.
 */
@Controller
public class HomeController {

    private static final String HEADER_KEY = "Content-Disposition";
    private static final String HEADER_VALUE = "attachment; filename=\"%s\"";
    private static final String TEMPLATE_FILE_NAME = "processed_%s";

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public void upload(@RequestParam("file") MultipartFile file,
                       @RequestParam("operation") String operation,
                       @RequestParam("isAscii") boolean isAscii,
                          HttpServletResponse response) {

        BufferedInputStream inputStream = null;
        PrintWriter outputStream = null;
        InputStream fileInputStream = null;

        if(!file.isEmpty()) {

            try {

                response.setHeader(HEADER_KEY, String.format(HEADER_VALUE, String.format(TEMPLATE_FILE_NAME,file.getOriginalFilename())));

                fileInputStream = file.getInputStream();

                inputStream = new BufferedInputStream(fileInputStream);
                outputStream = new PrintWriter(response.getOutputStream());

                StreamProcessor sp = new StreamProcessor(inputStream, outputStream, isAscii, operation.equals("encode"));
                sp.process2();

                outputStream.flush();
                sp.close();

                response.flushBuffer();

            } catch (FileNotFoundException e) {
                System.err.println("File not found - " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Error during io - " + e.getMessage());
            } finally {

                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
                IOUtils.closeQuietly(fileInputStream);
            }
        }
    }

    @RequestMapping(value = "/text", method = RequestMethod.POST)
    @ResponseBody
    public String textProcess(@RequestParam("text") String text,
                              @RequestParam("operation") String operation,
                              @RequestParam("isAscii") boolean isAscii) {

        InputStream inputStream = null;
        PrintWriter outputStream = null;
        StringWriter stringWriter = null;

        try {

            inputStream = IOUtils.toInputStream(text, "US-ASCII");
            stringWriter = new StringWriter();
            outputStream = new PrintWriter(stringWriter);

            StreamProcessor sp = new StreamProcessor(inputStream, outputStream, isAscii, operation.equals("encode"));
            sp.process2();

            outputStream.flush();
            sp.close();

            return stringWriter.toString();

        } catch(IOException e) {
            System.err.println("Error during io");
        } finally {

            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(stringWriter);
        }

        return "";
    }
}
