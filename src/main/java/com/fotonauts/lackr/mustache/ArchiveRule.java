package com.fotonauts.lackr.mustache;

import java.io.UnsupportedEncodingException;

import com.fotonauts.lackr.InterpolrFrontendRequest;
import com.fotonauts.lackr.backend.LackrBackendRequest;
import com.fotonauts.lackr.interpolr.Chunk;
import com.fotonauts.lackr.interpolr.DataChunk;
import com.fotonauts.lackr.interpolr.Document;
import com.fotonauts.lackr.interpolr.MarkupDetectingRule;

public class ArchiveRule extends MarkupDetectingRule {

    public ArchiveRule() {
        super("<script type=\"vnd.fotonauts/picordata\" id=\"*\">*</script><!-- END OF ARCHIVE -->");
    }

    @Override
    public Chunk substitute(byte[] buffer, int start, int[] boundPairs, int stop, Object context) {
        LackrBackendRequest request = (LackrBackendRequest) context;
        try {
            String archiveId = new String(buffer, boundPairs[0], boundPairs[1] - boundPairs[0], "UTF-8");
            InterpolrFrontendRequest front = (InterpolrFrontendRequest) request.getFrontendRequest();
            Document inner = front.getInterpolr().parse(buffer, boundPairs[2], boundPairs[3], request);
            front.getMustacheContext().registerArchive(archiveId, inner);

            return new Document(new Chunk[] { new DataChunk(buffer, start, boundPairs[2]), inner,
                    new DataChunk(buffer, boundPairs[3], stop) });
        } catch (UnsupportedEncodingException e) {
            /* nope */
            throw new RuntimeException(e);
        }
    }
}
