/*
 * -----------------------------------------------------------------------
 * Copyright © 2013-2014 Meno Hochschild, <http://www.menodata.de/>
 * -----------------------------------------------------------------------
 * This file (DaytimeConnector.java) is part of project Time4J.
 *
 * Time4J is free software: You can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Time4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Time4J. If not, see <http://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------
 */

package net.time4j.clock;

import net.time4j.Moment;
import net.time4j.PatternType;
import net.time4j.PlainDate;
import net.time4j.PlainTime;
import net.time4j.engine.AttributeQuery;
import net.time4j.format.Attributes;
import net.time4j.format.ChronoParser;
import net.time4j.format.ParseLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.ParseException;


/**
 * <p>Represents a connection to a DAYTIME-server following the old norm
 * RFC 867. </p>
 *
 * <p>Note that the format of the server reply is not specified by the
 * protocol. Furthermore, many internet time servers have stopped to
 * support this old protocol. Actually in year 2014, at least the
 * addresses &quot;time.nist.gov&quot; and &quot;time.ien.it&quot;
 * are still working. Applications cannot expect more than second
 * precision at best. </p>
 *
 * @author      Meno Hochschild
 * @since       2.1
 * @concurrency <threadsafe>
 */
/*[deutsch]
 * <p>Repr&auml;sentiert eine Verbindung zu einem DAYTIME-Server nach der
 * alten Norm RFC 867. </p>
 *
 * <p>Hinweis: Das Format der Server-Antwort ist ein unspezifizierter String.
 * Viele Uhrzeit-Server bieten inzwischen keine Unterst&uuml;tzung mehr.
 * Aktuell im Jahr 2014 funktionieren wenigstens noch die Adressen
 * &quot;time.nist.gov&quot; und &quot;time.ien.it&quot;. Mehr als
 * Sekundengenauigkeit ist nicht zu erwarten. </p>
 *
 * @author      Meno Hochschild
 * @since       2.1
 * @concurrency <threadsafe>
 */
public class DaytimeConnector
    extends NetTimeConnector<NetTimeConfiguration> {

    //~ Statische Felder/Initialisierungen --------------------------------

    private static final ChronoParser<PlainDate> MJD_PARSER =
        PlainDate.localFormatter("ggggg", PatternType.CLDR);
    private static final ChronoParser<PlainTime> TIME_PARSER =
        PlainTime.localFormatter("HH:mm:ss", PatternType.CLDR);

    private static final ChronoParser<Moment> NIST_PARSER =
        new ChronoParser<Moment>() {
            @Override
            public Moment parse(
                CharSequence text,
                ParseLog status,
                AttributeQuery attrs
            ) {
                int offset = 0;

                if (text.charAt(0) == '\n') {
                    offset = 1;
                }

                PlainDate date =
                    MJD_PARSER.parse(
                        text.subSequence(offset, 5 + offset),
                        status,
                        attrs);
                status.setPosition(0);
                PlainTime time =
                    TIME_PARSER.parse(
                        text.subSequence(15 + offset, 23 + offset),
                        status,
                        attrs);
                return date.at(time).atUTC();
            }
        };

    private static final DaytimeConnector NIST_CONNECTOR =
        new DaytimeConnector("time.nist.gov", NIST_PARSER);

    //~ Instanzvariablen --------------------------------------------------

    private final ChronoParser<Moment> parser;

    //~ Konstruktoren -----------------------------------------------------

    /**
     * <p>Gets an instance which supports the NIST-servers using
     * the address &quot;time.nist.gov&quot; and a specific format. </p>
     *
     * <p>The format is documented at
     * <a href="http://www.nist.gov/">www.nist.gov</a>. </p>
     */
    /*[deutsch]
     * <p>Liefert eine Instanz, die die NIST-Server mit der allgemeinen
     * Adresse &quot;time.nist.gov&quot; und deren spezifisches
     * Format verwendet. </p>
     *
     * <p>Das Format ist auf der Webseite
     * <a href="http://www.nist.gov/">www.nist.gov</a> dokumentiert. </p>
     */
    public static DaytimeConnector ofNIST() {

        return NIST_CONNECTOR;

    }

    /**
     * <p>Creates a new instance which uses the given time server on the
     * port 13. </p>
     *
     * @param   server  time server address (example: &quot;time.nist.gov&quot;)
     * @param   parser  object interpreting the server reply
     */
    /*[deutsch]
     * <p>Erzeugt eine neue Instanz, die den angegebenen Uhrzeit-Server
     * auf Port 13  benutzt. </p>
     *
     * @param   server  time server address (example: &quot;time.nist.gov&quot;)
     * @param   parser  object interpreting the server reply
     */
    public DaytimeConnector(
        String server,
        ChronoParser<Moment> parser
    ) {
        super(new SimpleDaytimeConfiguration(server));

        if (parser == null) {
            throw new NullPointerException(
                "Missing parser for translating any server reply.");
        }

        this.parser = parser;

    }

    //~ Methoden ----------------------------------------------------------

    /**
     * <p>Tries to get the raw server timestamp as original string. </p>
     *
     * @return  unparsed server reply
     * @throws  IOException if connection fails
     * @since   2.1
     */
    /*[deutsch]
     * <p>Versucht, den Original-Server-Zeitstempel zu lesen. </p>
     *
     * @return  unparsed server reply
     * @throws  IOException if connection fails
     * @since   2.1
     */
    public String getRawTimestamp() throws IOException {

        final NetTimeConfiguration config = this.getNetTimeConfiguration();

        String address = config.getTimeServerAddress();
        int port = config.getTimeServerPort();
        int timeout = config.getConnectionTimeout();

        return getDaytimeReply(address, port, timeout);

    }

    @Override
    protected Moment doConnect()
        throws ParseException, IOException {

        final NetTimeConfiguration config = this.getNetTimeConfiguration();

        String address = config.getTimeServerAddress();
        int port = config.getTimeServerPort();
        int timeout = config.getConnectionTimeout();

        String time = getDaytimeReply(address, port, timeout);
        this.log("DAYTIME-Server connected: ", time);

        return this.parser.parse(time, new ParseLog(), Attributes.empty());

    }

    @Override
    protected Class<NetTimeConfiguration> getConfigurationType() {

        return NetTimeConfiguration.class;

    }

    // Original-Antwort eines älteren Uhrzeit-Servers holen (RFC 867)
    private static String getDaytimeReply(
        String address,
        int port,
        int timeout
    ) throws IOException {

        IOException ioe = null;
        Socket socket = null;
        StringBuilder sb = null;

        try {

            socket = new Socket(address, port);
            socket.setSoTimeout(timeout * 1000);

            InputStream is = socket.getInputStream();
            InputStreamReader ir = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(ir);

            int len = 0;
            char[] chars = new char[100];
            sb = new StringBuilder();

            while ((len = br.read(chars)) != -1) {
                sb.append(chars, 0, len);
            }

            is.close();
            ir.close();
            br.close();

        } catch (IOException ex) {

            ioe = ex;

        } finally {

            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                System.err.println(
                    "Ignored exception while closing time server socket: "
                    + ex.getMessage()
                );
                ex.printStackTrace(System.err);
            }

        }

        if (ioe == null) {
            return sb.toString();
        } else {
            throw ioe;
        }

    }

    //~ Innere Klassen ----------------------------------------------------

    private static class SimpleDaytimeConfiguration
        implements NetTimeConfiguration {

        //~ Instanzvariablen ----------------------------------------------

        private final String server;

        //~ Konstruktoren -------------------------------------------------

        SimpleDaytimeConfiguration(String server) {
            super();

            if (server == null) {
                throw new NullPointerException("Missing time server address.");
            }

            this.server = server;

        }

        //~ Methoden ------------------------------------------------------

        @Override
        public String getTimeServerAddress() {

            return this.server;

        }

        @Override
        public int getTimeServerPort() {

            return 13;

        }

        @Override
        public int getConnectionTimeout() {

            return DEFAULT_CONNECTION_TIMEOUT;

        }

        @Override
        public int getClockShiftWindow() {

            return 0;

        }

        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();
            sb.append("SimpleDaytimeConfiguration:[server=");
            sb.append(this.server);
            sb.append(",port=");
            sb.append(this.getTimeServerPort());
            sb.append(']');
            return sb.toString();

        }

    }

}
