package com.fotonauts.lackr.mustache;

import java.io.IOException;
import java.io.OutputStream;

import com.fotonauts.lackr.BackendRequest;

public class ArchiveChunk extends ParsedJsonChunk {

    private String id;

    public ArchiveChunk(String id, byte[] buffer, int start, int stop, BackendRequest request) {
        super(buffer, start, stop, request);
        this.request = request;
        this.id = id;
    }

    @Override
    public int length() {
        return inner.length();
    }

    @Override
    public void writeTo(OutputStream stream) throws IOException {
        inner.writeTo(stream);
    }

    @Override
    public String toDebugString() {
        return "[ARCHIVE: " + id + " ]";
    }

    @Override
    public void check() {
        inner.check();
        request.getFrontendRequest().getMustacheContext().getArchives().put(id, parse());
    }
    
}