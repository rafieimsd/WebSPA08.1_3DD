package net.seleucus.wsp.db;

import java.nio.CharBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.seleucus.wsp.crypto.WebSpaEncoder;
import org.apache.commons.lang3.StringUtils;

public class WSPassPhrases {

    private final static Logger LOGGER = LoggerFactory.getLogger(WSPassPhrases.class);

    private Connection wsConnection;

    protected WSPassPhrases(Connection wsConnection) {

        this.wsConnection = wsConnection;

    }

    public synchronized boolean isPPIDInUse(int ppID) {

        boolean idExists = false;

        if (ppID > 0) {

            String sqlidLookup = "SELECT PPID FROM PASSPHRASES;";

            try {

                Statement stmt = wsConnection.createStatement();
                ResultSet rs = stmt.executeQuery(sqlidLookup);

                while (rs.next()) {
                    int dbPpID = rs.getInt(1);

                    if (dbPpID == ppID) {
                        idExists = true;
                        break;
                    }
                }

                rs.close();
                stmt.close();

            } catch (SQLException ex) {

                LOGGER.error("Is PPID in Use - A Database exception has occured: {}.", ex.getMessage());

            }

        } // ppID > 0 

        return idExists;
    }

    public synchronized boolean isPassPhraseInUse(CharSequence passSeq) {

        boolean passExists = false;

        String sqlPassPhrases = "SELECT PASSPHRASE FROM PASSPHRASES;";
        try {
            Statement stmt = wsConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sqlPassPhrases);

            while (rs.next()) {

                char[] dbPassPhraseArray = rs.getString(1).toCharArray();
                CharSequence dbPassSeq = CharBuffer.wrap(dbPassPhraseArray);

                if (dbPassSeq.equals(passSeq)) {

                    passExists = true;
                    break;

                }

            }	// while loop...

            rs.close();
            stmt.close();

        } catch (SQLException ex) {

            LOGGER.error("Is Pass-Phrase in Use - A Database exception has occured: {}.", ex.getMessage());

        }

        return passExists;

    }

    public synchronized boolean getActivationStatus(int usID) {

        boolean activationStatus = false;

        if (usID > 0) {

            String sqlActivationLookup = "SELECT ACTIVE FROM USERS WHERE USID = ? ;";
            try {
                PreparedStatement psPassPhrase = wsConnection.prepareStatement(sqlActivationLookup);
                psPassPhrase.setInt(1, usID);
                ResultSet rs = psPassPhrase.executeQuery();

                if (rs.next()) {
                    activationStatus = rs.getBoolean(1);
                }

                rs.close();
                psPassPhrase.close();

            } catch (SQLException ex) {

                LOGGER.error("Get Activation Status - A Database exception has occured: {}.", ex.getMessage());

            }

        } // ppID > 0

        return activationStatus;
    }

    public synchronized String getActivationStatusString(final int usID) {

        StringBuilder outputStatusBuffer = new StringBuilder(Byte.MAX_VALUE);
        outputStatusBuffer.append("User with ID: ");
        outputStatusBuffer.append(usID);
        outputStatusBuffer.append(' ');

        String sqlActivationLookup = "SELECT ACTIVE FROM USERS WHERE USID = ? ;";

        PreparedStatement psPassPhrase;
        try {
            psPassPhrase = wsConnection.prepareStatement(sqlActivationLookup);
            psPassPhrase.setInt(1, usID);
            ResultSet rs = psPassPhrase.executeQuery();

            if (rs.next()) {

                boolean activationStatus = rs.getBoolean(1);

                if (activationStatus == true) {

                    outputStatusBuffer.append("is active");

                } else {

                    outputStatusBuffer.append("is in-active");

                }

            } else {

                outputStatusBuffer.append("does not exist");

            }

            rs.close();
            psPassPhrase.close();

        } catch (SQLException ex) {

            LOGGER.error("Get Activation Status String - A Database exception has occured: {}.", ex.getMessage());

        }

        return outputStatusBuffer.toString();

    }

    public synchronized boolean toggleUserActivation(int usID) {

        boolean success = false;

        if (usID > 0) {

            boolean currentActiveStatus = this.getActivationStatus(usID);
            boolean oppositeActiveStatus = !currentActiveStatus;

            String sqlUpdate = "UPDATE USERS SET ACTIVE = ? WHERE USID = ? ;";
            try {
                PreparedStatement ps = wsConnection.prepareStatement(sqlUpdate);
                ps.setBoolean(1, oppositeActiveStatus);
                ps.setInt(2, usID);

                ps.executeUpdate();

            } catch (SQLException ex) {

                LOGGER.error("Toggle User Activation - A Database exception has occured: {}.", ex.getMessage());

            }

        } // ppID > 0

        return success;

    }

    public synchronized int getPPIDFromRequest(final String webSpaRequest) {

        int output = -1;
        final String sqlPassPhrases = "SELECT PASSPHRASE, PPID FROM PASSPHRASES;";

        try {
            Statement stmt = wsConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sqlPassPhrases);

            while (rs.next()) {

                char[] dbPassPhraseArray = rs.getString(1).toCharArray();
                final int dbPPID = rs.getInt(2);
                CharSequence rawPassword = CharBuffer.wrap(dbPassPhraseArray);

                if (WebSpaEncoder.matches(rawPassword, webSpaRequest)) {

                    output = dbPPID;
                    break;

                }

            }	// while loop...

            rs.close();
            stmt.close();

        } catch (SQLException ex) {

            LOGGER.error("Get PPID From Request - A Database exception has occured: {}.", ex.getMessage());

        }

        return output;

    }

    public synchronized CharSequence getPassPhrase(final int ppID) {

        CharSequence rawPassPhrase = CharBuffer.wrap("<could-not-find-pass-phrase>".toCharArray());

        if (ppID > 0) {

            final String sqlPassPhraseLookup = "SELECT PASSPHRASE FROM PASSPHRASES WHERE PPID = ? ;";

            PreparedStatement psPassPhrase;
            try {
                psPassPhrase = wsConnection.prepareStatement(sqlPassPhraseLookup);
                psPassPhrase.setInt(1, ppID);
                ResultSet rs = psPassPhrase.executeQuery();

                if (rs.next()) {
                    char[] dbPassPhraseArray = rs.getString(1).toCharArray();
                    rawPassPhrase = CharBuffer.wrap(dbPassPhraseArray);
                }

                rs.close();
                psPassPhrase.close();

            } catch (SQLException ex) {

                LOGGER.error("Get Pass-Phrase - A Database exception has occured: {}.", ex.getMessage());

            }

        } // ppID > 0

        return rawPassPhrase;

    }

    public synchronized String getLastModifiedDate(final int ppID) {

        String lastModified = "0000-00-00 00:00:00.000";

        if (ppID > 0) {

            final String sqlModifiedLookup = "SELECT MODIFIED FROM PASSPHRASES WHERE PPID = ? ;";

            PreparedStatement psModified;
            try {
                psModified = wsConnection.prepareStatement(sqlModifiedLookup);
                psModified.setInt(1, ppID);
                ResultSet rs = psModified.executeQuery();

                if (rs.next()) {
                    lastModified = rs.getString(1).substring(0, 23);
                }

                rs.close();
                psModified.close();

            } catch (SQLException ex) {

                LOGGER.error("Get Last Modified Date - A Database exception has occured: {}.", ex.getMessage());

            }

        } // ppID > 0

        return lastModified;
    }

    public synchronized boolean updatePassPhrase(final int ppID, final CharSequence passSeq) {

        boolean success = false;

        if (ppID > 0) {

            final String passPhraseUpdate = "UPDATE PASSPHRASES SET PASSPHRASE = ? , MODIFIED = CURRENT_TIMESTAMP WHERE PPID = ? ;";

            try {
                PreparedStatement ps = wsConnection.prepareStatement(passPhraseUpdate);
                ps.setString(1, passSeq.toString());
                ps.setInt(2, ppID);

                int rowCount = ps.executeUpdate();

                if (rowCount == 1) {
                    success = true;
                }

            } catch (SQLException ex) {

                LOGGER.error("Update Pass-Phrase - A Database exception has occured: {}.", ex.getMessage());

            }

        }

        return success;

    }

    public synchronized String showUserPassphrases(final int usID) {

        StringBuffer resultsBuffer = new StringBuffer();
        resultsBuffer.append('\n');
        resultsBuffer.append("Users Passphrases:");
        resultsBuffer.append('\n');
        resultsBuffer.append("___________________________________________________________");
        resultsBuffer.append('\n');
        resultsBuffer.append(StringUtils.rightPad("ID", 8));
        resultsBuffer.append(StringUtils.rightPad("rawPass", 14));
//        resultsBuffer.append(StringUtils.rightPad("Full Name", 24));
//        resultsBuffer.append(StringUtils.rightPad("Last Modified", 25));
        resultsBuffer.append('\n');
        resultsBuffer.append("-----------------------------------------------------------");
        resultsBuffer.append('\n');

        final String sqlPassPhraseLookup = "SELECT ppid,PASSPHRASE FROM  PASSPHRASEs WHERE usID = ? ;";

        PreparedStatement psPassPhrase;
        try {
            psPassPhrase = wsConnection.prepareStatement(sqlPassPhraseLookup);
            psPassPhrase.setInt(1, usID);
            ResultSet rs = psPassPhrase.executeQuery();

            while (rs.next()) {
                resultsBuffer.append(StringUtils.rightPad(rs.getString(1), 8));
                resultsBuffer.append(StringUtils.rightPad(rs.getString(2), 14));
//                resultsBuffer.append(StringUtils.rightPad(StringUtils.abbreviate(rs.getString(3), 23), 24));
//                resultsBuffer.append(rs.getString(4).substring(0, 23));
                resultsBuffer.append('\n');
            }
            resultsBuffer.append("___________________________________________________________");
            resultsBuffer.append('\n');

            rs.close();
            psPassPhrase.close();

        } catch (SQLException ex) {

            LOGGER.error("User Show - A Database error occured: {}", ex.getMessage());

        }

        return resultsBuffer.toString();
    }

}
