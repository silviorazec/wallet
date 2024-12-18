package com.recargapay.code.assessment.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

public class CapturingResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream captureBuffer = new ByteArrayOutputStream();
    private PrintWriter writer;
    private ServletOutputStream outputStream;

    public CapturingResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        if (writer != null) {
            throw new IllegalStateException("getWriter() has already been called on this response.");
        }

        try {
            outputStream = new CaptureServletOutputStream(captureBuffer, super.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to get OutputStream", e);
        }

        return outputStream;
    }

    @Override
    public PrintWriter getWriter() {
        if (outputStream != null) {
            throw new IllegalStateException("getOutputStream() has already been called on this response.");
        }

        if (writer == null) {
            writer = new PrintWriter(captureBuffer, true);
        }

        return writer;
    }

    public String getCapturedBody() {
        return captureBuffer.toString();
    }

    private static class CaptureServletOutputStream extends ServletOutputStream {

        private final ByteArrayOutputStream captureBuffer;
        private final ServletOutputStream originalOutputStream;

        public CaptureServletOutputStream(ByteArrayOutputStream captureBuffer, ServletOutputStream originalOutputStream) {
            this.captureBuffer = captureBuffer;
            this.originalOutputStream = originalOutputStream;
        }

        @Override
        public void write(int b) throws IOException {
            captureBuffer.write(b);
            originalOutputStream.write(b);
        }

        @Override
        public boolean isReady() {
            return originalOutputStream.isReady();
        }

        @Override
        public void setWriteListener(WriteListener listener) {
            originalOutputStream.setWriteListener(listener);
        }
    }
}
