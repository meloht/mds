package com.htc.mds.blobstorage;

import org.springframework.core.io.InputStreamSource;

import java.io.IOException;
import java.io.InputStream;

public class BlobInputStreamSource implements InputStreamSource {

    private BlobStorageClient _blobStorageClient;
    private String _guid;

    public BlobInputStreamSource(BlobStorageClient client, String guid) {
        _blobStorageClient = client;
        _guid = guid;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return _blobStorageClient.getInputStream(_guid);
    }
}
