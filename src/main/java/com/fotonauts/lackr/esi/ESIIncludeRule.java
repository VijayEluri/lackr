package com.fotonauts.lackr.esi;

import java.io.UnsupportedEncodingException;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;

import com.fotonauts.lackr.LackrContentExchange;
import com.fotonauts.lackr.interpolr.Chunk;
import com.fotonauts.lackr.interpolr.ConstantChunk;
import com.fotonauts.lackr.interpolr.MarkupDetectingRule;
import com.fotonauts.lackr.interpolr.Rule;

abstract public class ESIIncludeRule extends MarkupDetectingRule implements Rule {

	protected static ConstantChunk NULL_CHUNK = new ConstantChunk("null".getBytes());
	

	public ESIIncludeRule(String markup) {
		super(markup);
    }

	protected String getMimeType(LackrContentExchange exchange) {
		return exchange.getResponseFields().getStringField(HttpHeaders.CONTENT_TYPE);
	}

	@Override
    public Chunk substitute(byte[] buffer, int start, int stop, Object context) {
		LackrContentExchange exchange = (LackrContentExchange) context;
		String url = null ;
        try {
	        url = new String(buffer, start, stop - start, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        	// nope, thank you
        }
        LackrContentExchange sub = exchange.getLackrRequest().scheduleUpstreamRequest(url, HttpMethods.GET, null, exchange.getURI(), getSyntaxIdentifier());
        return new ExchangeChunk(sub, this);        
    }

	public abstract String getSyntaxIdentifier();

	public abstract Chunk filterDocumentAsChunk(LackrContentExchange exchange);
}
