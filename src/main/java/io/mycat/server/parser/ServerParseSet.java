/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese
 * opensource volunteers. you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Any questions about this component can be directed to it's project Web address
 * https://code.google.com/p/opencloudb/.
 *
 */
package io.mycat.server.parser;

import io.mycat.route.parser.util.ParseUtil;

/**
 * @author mycat
 */
public final class ServerParseSet {

    public static final int OTHER = -1;
    public static final int AUTOCOMMIT_ON = 1;
    public static final int AUTOCOMMIT_OFF = 2;
    public static final int TX_READ_UNCOMMITTED = 3;
    public static final int TX_READ_COMMITTED = 4;
    public static final int TX_REPEATED_READ = 5;
    public static final int TX_SERIALIZABLE = 6;
    public static final int NAMES = 7;
    public static final int CHARACTER_SET_CLIENT = 8;
    public static final int CHARACTER_SET_CONNECTION = 9;
    public static final int CHARACTER_SET_RESULTS = 10;
    public static final int XA_FLAG_ON = 11;
    public static final int XA_FLAG_OFF = 12;
    public static final int CHARACTER_SET_NAME = 13;

    private static final int VALUE_ON = 1;
    private static final int VALUE_OFF = 0;

    public static int parse(String stmt, int offset) {
        if (!ParseUtil.isSpace(stmt.charAt(offset))) {
            return OTHER;
        }
        while (stmt.length() > ++offset) {
            switch (stmt.charAt(offset)) {
                case ' ':
                case '\r':
                case '\n':
                case '\t':
                    continue;
                case '/':
                case '#':
                    offset = ParseUtil.comment(stmt, offset);
                    continue;
                case 'A':
                case 'a':
                    return autocommit(stmt, offset);
                case 'C':
                case 'c':
                    return character(stmt, offset);
                case 'N':
                case 'n':
                    return names(stmt, offset);
                case 'S':
                case 's':
                    return session(stmt, offset);
                case 'X':
                case 'x':
                    return xaFlag(stmt, offset);
                case '@':
                    return parseAt(stmt, offset);
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    // set xa=1
    private static int xaFlag(String stmt, int offset) {
        if (stmt.length() > offset + 1) {
            char c1 = stmt.charAt(++offset);
            if ((c1 == 'A' || c1 == 'a')) {
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                        case '\r':
                        case '\n':
                        case '\t':
                            continue;
                        case '=':
                            int value = parseValue(stmt, offset);
                            if (value == VALUE_ON) {
                                return XA_FLAG_ON;
                            } else if (value == VALUE_OFF) {
                                return XA_FLAG_OFF;
                            } else {
                                return OTHER;
                            }
                        default:
                            return OTHER;
                    }
                }
            }
        }
        return OTHER;
    }

    //AUTOCOMMIT(' '=)
    private static int autocommit(String stmt, int offset) {
        if (stmt.length() > offset + 9) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            char c8 = stmt.charAt(++offset);
            char c9 = stmt.charAt(++offset);
            if ((c1 == 'U' || c1 == 'u') && (c2 == 'T' || c2 == 't') &&
                    (c3 == 'O' || c3 == 'o') && (c4 == 'C' || c4 == 'c') &&
                    (c5 == 'O' || c5 == 'o') && (c6 == 'M' || c6 == 'm') &&
                    (c7 == 'M' || c7 == 'm') && (c8 == 'I' || c8 == 'i') &&
                    (c9 == 'T' || c9 == 't')) {
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                        case '\r':
                        case '\n':
                        case '\t':
                            continue;
                        case '=':
                            int value = parseValue(stmt, offset);
                            if (value == VALUE_ON) {
                                return AUTOCOMMIT_ON;
                            } else if (value == VALUE_OFF) {
                                return AUTOCOMMIT_OFF;
                            } else {
                                return OTHER;
                            }
                        default:
                            return OTHER;
                    }
                }
            }
        }
        return OTHER;
    }

    private static int parseValue(String stmt, int offset) {
        for (; ; ) {
            offset++;
            if (stmt.length() <= offset) {
                return OTHER;
            }
            switch (stmt.charAt(offset)) {
                case ' ':
                case '\r':
                case '\n':
                case '\t':
                    continue;
                case '1':
                    if (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset))) {
                        return VALUE_ON;
                    } else {
                        return OTHER;
                    }
                case '0':
                    if (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset))) {
                        return VALUE_OFF;
                    } else {
                        return OTHER;
                    }
                case 'O':
                case 'o':
                    return parseValueO(stmt, offset);
                default:
                    return OTHER;
            }
        }
    }

    private static int parseValueO(String stmt, int offset) {
        if (stmt.length() > ++offset) {
            switch (stmt.charAt(offset)) {
                case 'N':
                case 'n':
                    if (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset))) {
                        return VALUE_ON;
                    } else {
                        return OTHER;
                    }
                case 'F':
                case 'f':
                    return parseValueOff(stmt, offset);
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    // SET AUTOCOMMIT = OFF
    private static int parseValueOff(String stmt, int offset) {
        if (stmt.length() > ++offset) {
            switch (stmt.charAt(offset)) {
                case 'F':
                case 'f':
                    if (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset))) {
                        return VALUE_OFF;
                    } else {
                        return OTHER;
                    }
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    // SET NAMES' '
    private static int names(String stmt, int offset) {
        if (stmt.length() > offset + 5) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            if ((c1 == 'A' || c1 == 'a') && (c2 == 'M' || c2 == 'm') &&
                    (c3 == 'E' || c3 == 'e') && (c4 == 'S' || c4 == 's') &&
                    ParseUtil.isSpace(stmt.charAt(++offset))) {
                return (offset << 8) | NAMES;
            }
        }
        return OTHER;
    }

    // SET CHARACTER
    private static int character(String stmt, int offset) {
        if (stmt.length() > offset + 9) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            char c8 = stmt.charAt(++offset);
            char c9 = stmt.charAt(++offset);
            if ((c1 == 'H' || c1 == 'h') && (c2 == 'A' || c2 == 'a') && (c3 == 'R' || c3 == 'r') &&
                    (c4 == 'A' || c4 == 'a') && (c5 == 'C' || c5 == 'c') && (c6 == 'T' || c6 == 't') &&
                    (c7 == 'E' || c7 == 'e') && (c8 == 'R' || c8 == 'r')) {
                switch (c9) {
                    case ' ':
                    case '\r':
                    case '\n':
                    case '\t':
                        return characterSetName(stmt, offset);
                    case '_':
                        return characterSet(stmt, offset);
                    default:
                        return OTHER;
                }
            }
        }
        return OTHER;
    }

    // SET CHARACTER SET ''
    private static int characterSetName(String stmt, int offset) {
        while (stmt.length() > ++offset) {
            switch (stmt.charAt(offset)) {
                case ' ':
                case '\r':
                case '\n':
                case '\t':
                    continue;
                case 'S':
                case 's':
                    if (stmt.length() > offset + 4) {
                        char c2 = stmt.charAt(++offset);
                        char c3 = stmt.charAt(++offset);
                        if ((c2 == 'E' || c2 == 'e') && (c3 == 'T' || c3 == 't') && ParseUtil.isSpace(stmt.charAt(++offset))) {
                            return (offset << 8) | CHARACTER_SET_NAME;
                        }
                    }
                    return OTHER;
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    // SET CHARACTER_SET_
    private static int characterSet(String stmt, int offset) {
        if (stmt.length() > offset + 5) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            if ((c1 == 'S' || c1 == 's') && (c2 == 'E' || c2 == 'e') &&
                    (c3 == 'T' || c3 == 't') && (c4 == '_')) {
                switch (c5) {
                    case 'R':
                    case 'r':
                        return characterSetResults(stmt, offset);
                    case 'C':
                    case 'c':
                        return characterSetC(stmt, offset);
                    default:
                        return OTHER;
                }
            }
        }
        return OTHER;
    }

    // SET CHARACTER_SET_RESULTS =
    private static int characterSetResults(String stmt, int offset) {
        if (stmt.length() > offset + 6) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            if ((c1 == 'E' || c1 == 'e') && (c2 == 'S' || c2 == 's') && (c3 == 'U' || c3 == 'u') &&
                    (c4 == 'L' || c4 == 'l') && (c5 == 'T' || c5 == 't') && (c6 == 'S' || c6 == 's')) {
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                        case '\r':
                        case '\n':
                        case '\t':
                            continue;
                        case '=':
                            while (stmt.length() > ++offset) {
                                switch (stmt.charAt(offset)) {
                                    case ' ':
                                    case '\r':
                                    case '\n':
                                    case '\t':
                                        continue;
                                    default:
                                        return (offset << 8) | CHARACTER_SET_RESULTS;
                                }
                            }
                            return OTHER;
                        default:
                            return OTHER;
                    }
                }
            }
        }
        return OTHER;
    }

    // SET CHARACTER_SET_C
    private static int characterSetC(String stmt, int offset) {
        if (stmt.length() > offset + 1) {
            char c1 = stmt.charAt(++offset);
            switch (c1) {
                case 'o':
                case 'O':
                    return characterSetConnection(stmt, offset);
                case 'l':
                case 'L':
                    return characterSetClient(stmt, offset);
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    // SET CHARACTER_SET_CONNECTION =
    private static int characterSetConnection(String stmt, int offset) {
        if (stmt.length() > offset + 8) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            char c8 = stmt.charAt(++offset);
            if ((c1 == 'N' || c1 == 'n') && (c2 == 'N' || c2 == 'n') &&
                    (c3 == 'E' || c3 == 'e') && (c4 == 'C' || c4 == 'c') &&
                    (c5 == 'T' || c5 == 't') && (c6 == 'I' || c6 == 'i') &&
                    (c7 == 'O' || c7 == 'o') && (c8 == 'N' || c8 == 'n')) {
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                        case '\r':
                        case '\n':
                        case '\t':
                            continue;
                        case '=':
                            while (stmt.length() > ++offset) {
                                switch (stmt.charAt(offset)) {
                                    case ' ':
                                    case '\r':
                                    case '\n':
                                    case '\t':
                                        continue;
                                    default:
                                        return (offset << 8) | CHARACTER_SET_CONNECTION;
                                }
                            }
                            return OTHER;
                        default:
                            return OTHER;
                    }
                }
            }
        }
        return OTHER;
    }

    // SET CHARACTER_SET_CLIENT =
    private static int characterSetClient(String stmt, int offset) {
        if (stmt.length() > offset + 4) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            if ((c1 == 'I' || c1 == 'i') && (c2 == 'E' || c2 == 'e') &&
                    (c3 == 'N' || c3 == 'n') && (c4 == 'T' || c4 == 't')) {
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                        case '\r':
                        case '\n':
                        case '\t':
                            continue;
                        case '=':
                            while (stmt.length() > ++offset) {
                                switch (stmt.charAt(offset)) {
                                    case ' ':
                                    case '\r':
                                    case '\n':
                                    case '\t':
                                        continue;
                                    default:
                                        return (offset << 8) | CHARACTER_SET_CLIENT;
                                }
                            }
                            return OTHER;
                        default:
                            return OTHER;
                    }
                }
            }
        }
        return OTHER;
    }

    //SESSION +space
    private static int session(String stmt, int offset) {
        if (stmt.length() > offset + 7) {
            if (isSession(stmt, offset)) {
                offset = offset + 6;
                if (!ParseUtil.isSpace(stmt.charAt(++offset))) {
                    return OTHER;
                }
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                        case '\r':
                        case '\n':
                        case '\t':
                            continue;
                        case 'T':
                        case 't':
                            return transaction(stmt, offset);
                        case 'A':
                        case 'a':
                            return autocommit(stmt, offset);
                        default:
                            return OTHER;
                    }
                }
            }
        }
        return OTHER;
    }

    // set @@
    private static int parseAt(String stmt, int offset) {
        if (stmt.length() > ++offset && stmt.charAt(offset) == '@' && stmt.length() > ++offset) {
            switch (stmt.charAt(offset)) {
                case 'S':
                case 's':
                    return sessionDot(stmt, offset);
                case 'A':
                case 'a':
                    return autocommit(stmt, offset);
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    // SET @@SESSION.
    private static int sessionDot(String stmt, int offset) {
        if (stmt.length() > offset + 8) {
            if (isSession(stmt, offset)) {
                offset = offset + 6;
                if (stmt.charAt(++offset) == '.') {
                    switch (stmt.charAt(++offset)) {
                        case 'T':
                        case 't':
                            return parseTx(stmt, offset);
                        case 'A':
                        case 'a':
                            return autocommit(stmt, offset);
                        default:
                            return OTHER;
                    }
                }
            }
        }
        return OTHER;
    }

    // tx_isolation
    private static int parseTx(String stmt, int offset) {
        if (stmt.length() > offset + 4) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            if ((c1 == 'X' || c1 == 'x') && (c2 == '_') && (c3 == 'I' || c3 == 'i') && isIsolation(stmt, offset)) {
                offset = offset + 8;
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                        case '\r':
                        case '\n':
                        case '\t':
                            continue;
                        case '=':
                            return parserIsolationValue(stmt, offset, true);
                        default:
                            return OTHER;
                    }
                }
            }
        }
        return OTHER;
    }

    private static int parserIsolationValue(String stmt, int offset, boolean checkApostrophe) {
        while (stmt.length() > ++offset) {
            switch (stmt.charAt(offset)) {
                case ' ':
                case '\r':
                case '\n':
                case '\t':
                    continue;
                case '\'':
                    return parserLevel(stmt, offset + 1, checkApostrophe);
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    // SET SESSION TRANSACTION ISOLATION LEVEL
    private static int transaction(String stmt, int offset) {
        if (stmt.length() > offset + 11) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            char c8 = stmt.charAt(++offset);
            char c9 = stmt.charAt(++offset);
            char c10 = stmt.charAt(++offset);
            if ((c1 == 'R' || c1 == 'r') && (c2 == 'A' || c2 == 'a') && (c3 == 'N' || c3 == 'n') &&
                    (c4 == 'S' || c4 == 's') && (c5 == 'A' || c5 == 'a') && (c6 == 'C' || c6 == 'c') &&
                    (c7 == 'T' || c7 == 't') && (c8 == 'I' || c8 == 'i') && (c9 == 'O' || c9 == 'o') &&
                    (c10 == 'N' || c10 == 'n') && ParseUtil.isSpace(stmt.charAt(++offset))) {
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                        case '\r':
                        case '\n':
                        case '\t':
                            continue;
                        case 'I':
                        case 'i':
                            return isolation(stmt, offset);
                        default:
                            return OTHER;
                    }
                }
            }
        }
        return OTHER;
    }

    // SET SESSION TRANSACTION ISOLATION LEVEL || set @@session.isolation = ' '
    private static int isolation(String stmt, int offset) {
        if (stmt.length() > offset + 9) {
            if (isIsolation(stmt, offset)) {
                offset = offset + 8;
                if (!ParseUtil.isSpace(stmt.charAt(++offset))) {
                    return OTHER;
                }
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                        case '\r':
                        case '\n':
                        case '\t':
                            continue;
                        case 'L':
                        case 'l':
                            return level(stmt, offset);
                        default:
                            return OTHER;
                    }
                }
            }
        }
        return OTHER;
    }

    // SET SESSION TRANSACTION ISOLATION LEVEL' '
    private static int level(String stmt, int offset) {
        if (stmt.length() > offset + 5) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            if ((c1 == 'E' || c1 == 'e') && (c2 == 'V' || c2 == 'v') && (c3 == 'E' || c3 == 'e') &&
                    (c4 == 'L' || c4 == 'l') && ParseUtil.isSpace(stmt.charAt(++offset))) {
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                        case '\r':
                        case '\n':
                        case '\t':
                            continue;
                        case 'r':
                        case 'R':
                        case 's':
                        case 'S':
                            stmt = stmt.toUpperCase();
                            return parserLevel(stmt, offset, false);
                        default:
                            return OTHER;
                    }
                }
            }
        }
        return OTHER;
    }

    private static int parserLevel(String stmt, int offset, boolean checkApostrophe) {
        switch (stmt.charAt(offset)) {
            case 'R':
                return rCheck(stmt, offset, checkApostrophe);
            case 'S':
                return serializable(stmt, offset, checkApostrophe);
            default:
                return OTHER;
        }
    }

    // SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE
    private static int serializable(String stmt, int offset, boolean checkApostrophe) {
        if (stmt.length() > offset + 11) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            char c8 = stmt.charAt(++offset);
            char c9 = stmt.charAt(++offset);
            char c10 = stmt.charAt(++offset);
            char c11 = stmt.charAt(++offset);
            if ((c1 == 'E') && (c2 == 'R') && (c3 == 'I') && (c4 == 'A') && (c5 == 'L') && (c6 == 'I') && (c7 == 'Z') &&
                    (c8 == 'A') && (c9 == 'B') && (c10 == 'L') && (c11 == 'E')) {
                if (checkApostrophe && stmt.charAt(++offset) != '\'') {
                    return OTHER;
                }
                if ((stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
                    return TX_SERIALIZABLE;
                }
            }
        }
        return OTHER;
    }

    // READ' '|REPEATABLE
    private static int rCheck(String stmt, int offset, boolean checkApostrophe) {
        if (stmt.length() > ++offset) {
            switch (stmt.charAt(offset)) {
                case 'E':
                    return eCheck(stmt, offset, checkApostrophe);
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    // READ' '|REPEATABLE
    private static int eCheck(String stmt, int offset, boolean checkApostrophe) {
        if (stmt.length() > ++offset) {
            switch (stmt.charAt(offset)) {
                case 'A':
                    return aCheck(stmt, offset, checkApostrophe);
                case 'P':
                    return pCheck(stmt, offset, checkApostrophe);
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    // READ' '||READ-
    private static int aCheck(String stmt, int offset, boolean checkApostrophe) {
        if ((stmt.length() > offset + 2) && (stmt.charAt(++offset) == 'D')) {
            if (checkApostrophe) {
                if (stmt.charAt(++offset) != '-') {
                    return OTHER;
                }
                offset++;
            } else if (!ParseUtil.isSpace(stmt.charAt(++offset))) {
                return OTHER;
            } else {
                boolean find = false;
                while (!find && stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                        case '\r':
                        case '\n':
                        case '\t':
                            continue;
                        case 'C':
                        case 'U':
                            find = true;
                            break;
                        default:
                            return OTHER;
                    }
                }
            }
            switch (stmt.charAt(offset)) {
                case 'C':
                    return committed(stmt, offset, checkApostrophe);
                case 'U':
                    return uncommitted(stmt, offset, checkApostrophe);
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    // COMMITTED
    private static int committed(String stmt, int offset, boolean checkApostrophe) {
        if (stmt.length() > offset + 8) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            char c8 = stmt.charAt(++offset);
            if ((c1 == 'O') && (c2 == 'M') && (c3 == 'M') && (c4 == 'I') && (c5 == 'T') && (c6 == 'T') && (c7 == 'E') &&
                    (c8 == 'D')) {
                if (checkApostrophe && stmt.charAt(++offset) != '\'') {
                    return OTHER;
                }
                if ((stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
                    return TX_READ_COMMITTED;
                }
            }
        }
        return OTHER;
    }

    // UNCOMMITTED
    private static int uncommitted(String stmt, int offset, boolean checkApostrophe) {
        if (stmt.length() > offset + 10) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            char c8 = stmt.charAt(++offset);
            char c9 = stmt.charAt(++offset);
            char c10 = stmt.charAt(++offset);
            if ((c1 == 'N') && (c2 == 'C') && (c3 == 'O') && (c4 == 'M') && (c5 == 'M') && (c6 == 'I') && (c7 == 'T') &&
                    (c8 == 'T') && (c9 == 'E') && (c10 == 'D')) {
                if (checkApostrophe && stmt.charAt(++offset) != '\'') {
                    return OTHER;
                }
                if ((stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
                    return TX_READ_UNCOMMITTED;
                }
            }
        }
        return OTHER;
    }

    // REPEATABLE
    private static int pCheck(String stmt, int offset, boolean checkApostrophe) {
        if (stmt.length() > offset + 8) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            if ((c1 == 'E') && (c2 == 'A') && (c3 == 'T') && (c4 == 'A') && (c5 == 'B') && (c6 == 'L') && (c7 == 'E')) {
                if (checkApostrophe) {
                    if (stmt.charAt(++offset) != '-') {
                        return OTHER;
                    }
                    offset++;
                } else if (!ParseUtil.isSpace(stmt.charAt(++offset))) {
                    return OTHER;
                } else {
                    boolean find = false;
                    while (!find && stmt.length() > ++offset) {
                        switch (stmt.charAt(offset)) {
                            case ' ':
                            case '\r':
                            case '\n':
                            case '\t':
                                continue;
                            case 'R':
                            case 'r':
                                find = true;
                                break;
                            default:
                                return OTHER;
                        }
                    }
                }
                switch (stmt.charAt(offset)) {
                    case 'R':
                    case 'r':
                        return prCheck(stmt, offset, checkApostrophe);
                    default:
                        return OTHER;
                }
            }
        }
        return OTHER;
    }

    // READ
    private static int prCheck(String stmt, int offset, boolean checkApostrophe) {
        if (stmt.length() > offset + 3) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            if ((c1 == 'E') && (c2 == 'A') && (c3 == 'D')) {
                if (checkApostrophe && stmt.charAt(++offset) != '\'') {
                    return OTHER;
                }
                if ((stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
                    return TX_REPEATED_READ;
                }
            }
        }
        return OTHER;
    }

    private static boolean isIsolation(String stmt, int offset) {
        char c1 = stmt.charAt(++offset);
        char c2 = stmt.charAt(++offset);
        char c3 = stmt.charAt(++offset);
        char c4 = stmt.charAt(++offset);
        char c5 = stmt.charAt(++offset);
        char c6 = stmt.charAt(++offset);
        char c7 = stmt.charAt(++offset);
        char c8 = stmt.charAt(++offset);
        if ((c1 == 'S' || c1 == 's') && (c2 == 'O' || c2 == 'o') && (c3 == 'L' || c3 == 'l') && (c4 == 'A' || c4 == 'a') &&
                (c5 == 'T' || c5 == 't') && (c6 == 'I' || c6 == 'i') && (c7 == 'O' || c7 == 'o') &&
                (c8 == 'N' || c8 == 'n')) {
            return true;
        }
        return false;
    }

    private static boolean isSession(String stmt, int offset) {
        char c1 = stmt.charAt(++offset);
        char c2 = stmt.charAt(++offset);
        char c3 = stmt.charAt(++offset);
        char c4 = stmt.charAt(++offset);
        char c5 = stmt.charAt(++offset);
        char c6 = stmt.charAt(++offset);
        if ((c1 == 'E' || c1 == 'e') && (c2 == 'S' || c2 == 's') && (c3 == 'S' || c3 == 's') && (c4 == 'I' || c4 == 'i') &&
                (c5 == 'O' || c5 == 'o') && (c6 == 'N' || c6 == 'n')) {
            return true;
        }
        return false;
    }

}
