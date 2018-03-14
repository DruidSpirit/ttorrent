package com.turn.ttorrent.common;

import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.BEncoder;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.testng.Assert.*;

@Test
public class TorrentParserTest {

  private TorrentParser myTorrentParser;

  @BeforeMethod
  public void setUp() throws Exception {
    myTorrentParser = new TorrentParser();
  }

  public void parseTest() throws IOException, NoSuchAlgorithmException {
    final Map<String, BEValue> metadata = new HashMap<String, BEValue>();
    final HashMap<String, BEValue> infoTable = new HashMap<String, BEValue>();

    metadata.put("announce", new BEValue("http://localhost/announce"));

    infoTable.put("piece length", new BEValue(4));

    infoTable.put("pieces", new BEValue(new byte[100]));
    infoTable.put("name", new BEValue("test.file"));
    infoTable.put("length", new BEValue(19));

    metadata.put("info", new BEValue(infoTable));

    final TorrentMultiFileMetadata torrentMultiFileMetadata = myTorrentParser.parse(BEncoder.bencode(metadata).array());

    assertEquals(torrentMultiFileMetadata.getPieceLength(), 4);
    assertEquals(torrentMultiFileMetadata.getAnnounce(), "http://localhost/announce");
    assertEquals(torrentMultiFileMetadata.getDirectoryName(), "test.file");
    assertNull(torrentMultiFileMetadata.getAnnounceList());

    List<BEValue> announceList = new ArrayList<BEValue>();
    announceList.add(new BEValue(Collections.singletonList(new BEValue("http://localhost/announce"))));
    announceList.add(new BEValue(Collections.singletonList(new BEValue("http://second/announce"))));
    metadata.put("announce-list", new BEValue(announceList));

    final TorrentMultiFileMetadata torrentMultiFileMetadataWithAnnounceList = myTorrentParser.parse(BEncoder.bencode(metadata).array());

    final List<List<String>> actualAnnounceList = torrentMultiFileMetadataWithAnnounceList.getAnnounceList();
    assertNotNull(actualAnnounceList);
    assertEquals(actualAnnounceList.get(0).get(0), "http://localhost/announce");
    assertEquals(actualAnnounceList.get(1).get(0), "http://second/announce");

  }

  public void badBEPFormatTest() {
    try {
      myTorrentParser.parse("abcd".getBytes());
      fail("This method must throw invalid bencoding exception");
    } catch (InvalidBEncodingException e) {
      //it's okay
    } catch (NoSuchAlgorithmException e) {
      fail("", e);
    }
  }

  public void missingRequiredFieldTest() {
    Map<String, BEValue> map = new HashMap<String, BEValue>();
    map.put("info", new BEValue(new HashMap<String, BEValue>()));

    try {
      myTorrentParser.parse(BEncoder.bencode(map).array());
      fail("This method must throw invalid bencoding exception");
    } catch (InvalidBEncodingException e) {
      //it's okay
    } catch (NoSuchAlgorithmException e) {
      fail("", e);
    } catch (IOException e) {
      fail("", e);
    }
  }
}