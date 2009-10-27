// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 Lua.g 2009-10-19 10:13:34
 
	package org.luaj.vm2.luajc.antlr;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class LuaLexer extends Lexer {
    public static final int T__66=66;
    public static final int T__64=64;
    public static final int T__29=29;
    public static final int T__28=28;
    public static final int T__65=65;
    public static final int T__27=27;
    public static final int T__62=62;
    public static final int T__26=26;
    public static final int T__63=63;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int T__61=61;
    public static final int EOF=-1;
    public static final int T__60=60;
    public static final int FLOAT3=8;
    public static final int FLOAT2=7;
    public static final int FLOAT1=6;
    public static final int T__55=55;
    public static final int T__56=56;
    public static final int T__57=57;
    public static final int NAME=4;
    public static final int T__58=58;
    public static final int T__51=51;
    public static final int T__52=52;
    public static final int T__53=53;
    public static final int T__54=54;
    public static final int EXP=9;
    public static final int HEX=10;
    public static final int T__59=59;
    public static final int DecimalEscape=15;
    public static final int COMMENT=16;
    public static final int T__50=50;
    public static final int T__42=42;
    public static final int T__43=43;
    public static final int T__40=40;
    public static final int T__41=41;
    public static final int T__46=46;
    public static final int T__47=47;
    public static final int T__44=44;
    public static final int T__45=45;
    public static final int LINE_COMMENT=17;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int INT=5;
    public static final int CHARSTRING=12;
    public static final int LONGSTRING=13;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int NORMALSTRING=11;
    public static final int T__32=32;
    public static final int T__33=33;
    public static final int WS=18;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int NEWLINE=19;
    public static final int T__36=36;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int EscapeSequence=14;

    // delegates
    // delegators

    public LuaLexer() {;} 
    public LuaLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public LuaLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "Lua.g"; }

    // $ANTLR start "T__20"
    public final void mT__20() throws RecognitionException {
        try {
            int _type = T__20;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:7:7: ( ';' )
            // Lua.g:7:9: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__20"

    // $ANTLR start "T__21"
    public final void mT__21() throws RecognitionException {
        try {
            int _type = T__21;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:8:7: ( '=' )
            // Lua.g:8:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__21"

    // $ANTLR start "T__22"
    public final void mT__22() throws RecognitionException {
        try {
            int _type = T__22;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:9:7: ( 'do' )
            // Lua.g:9:9: 'do'
            {
            match("do"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__22"

    // $ANTLR start "T__23"
    public final void mT__23() throws RecognitionException {
        try {
            int _type = T__23;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:10:7: ( 'end' )
            // Lua.g:10:9: 'end'
            {
            match("end"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__23"

    // $ANTLR start "T__24"
    public final void mT__24() throws RecognitionException {
        try {
            int _type = T__24;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:11:7: ( 'while' )
            // Lua.g:11:9: 'while'
            {
            match("while"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__24"

    // $ANTLR start "T__25"
    public final void mT__25() throws RecognitionException {
        try {
            int _type = T__25;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:12:7: ( 'repeat' )
            // Lua.g:12:9: 'repeat'
            {
            match("repeat"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__25"

    // $ANTLR start "T__26"
    public final void mT__26() throws RecognitionException {
        try {
            int _type = T__26;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:13:7: ( 'until' )
            // Lua.g:13:9: 'until'
            {
            match("until"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__26"

    // $ANTLR start "T__27"
    public final void mT__27() throws RecognitionException {
        try {
            int _type = T__27;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:14:7: ( 'for' )
            // Lua.g:14:9: 'for'
            {
            match("for"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__27"

    // $ANTLR start "T__28"
    public final void mT__28() throws RecognitionException {
        try {
            int _type = T__28;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:15:7: ( ',' )
            // Lua.g:15:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__28"

    // $ANTLR start "T__29"
    public final void mT__29() throws RecognitionException {
        try {
            int _type = T__29;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:16:7: ( 'in' )
            // Lua.g:16:9: 'in'
            {
            match("in"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__29"

    // $ANTLR start "T__30"
    public final void mT__30() throws RecognitionException {
        try {
            int _type = T__30;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:17:7: ( 'function' )
            // Lua.g:17:9: 'function'
            {
            match("function"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__30"

    // $ANTLR start "T__31"
    public final void mT__31() throws RecognitionException {
        try {
            int _type = T__31;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:18:7: ( 'local' )
            // Lua.g:18:9: 'local'
            {
            match("local"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__31"

    // $ANTLR start "T__32"
    public final void mT__32() throws RecognitionException {
        try {
            int _type = T__32;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:19:7: ( 'if' )
            // Lua.g:19:9: 'if'
            {
            match("if"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__32"

    // $ANTLR start "T__33"
    public final void mT__33() throws RecognitionException {
        try {
            int _type = T__33;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:20:7: ( 'then' )
            // Lua.g:20:9: 'then'
            {
            match("then"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__33"

    // $ANTLR start "T__34"
    public final void mT__34() throws RecognitionException {
        try {
            int _type = T__34;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:21:7: ( 'elseif' )
            // Lua.g:21:9: 'elseif'
            {
            match("elseif"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__34"

    // $ANTLR start "T__35"
    public final void mT__35() throws RecognitionException {
        try {
            int _type = T__35;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:22:7: ( 'else' )
            // Lua.g:22:9: 'else'
            {
            match("else"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__35"

    // $ANTLR start "T__36"
    public final void mT__36() throws RecognitionException {
        try {
            int _type = T__36;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:23:7: ( 'return' )
            // Lua.g:23:9: 'return'
            {
            match("return"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__36"

    // $ANTLR start "T__37"
    public final void mT__37() throws RecognitionException {
        try {
            int _type = T__37;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:24:7: ( 'break' )
            // Lua.g:24:9: 'break'
            {
            match("break"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__37"

    // $ANTLR start "T__38"
    public final void mT__38() throws RecognitionException {
        try {
            int _type = T__38;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:25:7: ( '.' )
            // Lua.g:25:9: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__38"

    // $ANTLR start "T__39"
    public final void mT__39() throws RecognitionException {
        try {
            int _type = T__39;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:26:7: ( ':' )
            // Lua.g:26:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__39"

    // $ANTLR start "T__40"
    public final void mT__40() throws RecognitionException {
        try {
            int _type = T__40;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:27:7: ( 'nil' )
            // Lua.g:27:9: 'nil'
            {
            match("nil"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__40"

    // $ANTLR start "T__41"
    public final void mT__41() throws RecognitionException {
        try {
            int _type = T__41;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:28:7: ( 'false' )
            // Lua.g:28:9: 'false'
            {
            match("false"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__41"

    // $ANTLR start "T__42"
    public final void mT__42() throws RecognitionException {
        try {
            int _type = T__42;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:29:7: ( 'true' )
            // Lua.g:29:9: 'true'
            {
            match("true"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__42"

    // $ANTLR start "T__43"
    public final void mT__43() throws RecognitionException {
        try {
            int _type = T__43;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:30:7: ( '...' )
            // Lua.g:30:9: '...'
            {
            match("..."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__43"

    // $ANTLR start "T__44"
    public final void mT__44() throws RecognitionException {
        try {
            int _type = T__44;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:31:7: ( '(' )
            // Lua.g:31:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__44"

    // $ANTLR start "T__45"
    public final void mT__45() throws RecognitionException {
        try {
            int _type = T__45;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:32:7: ( ')' )
            // Lua.g:32:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__45"

    // $ANTLR start "T__46"
    public final void mT__46() throws RecognitionException {
        try {
            int _type = T__46;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:33:7: ( '[' )
            // Lua.g:33:9: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__46"

    // $ANTLR start "T__47"
    public final void mT__47() throws RecognitionException {
        try {
            int _type = T__47;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:34:7: ( ']' )
            // Lua.g:34:9: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__47"

    // $ANTLR start "T__48"
    public final void mT__48() throws RecognitionException {
        try {
            int _type = T__48;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:35:7: ( '{' )
            // Lua.g:35:9: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__48"

    // $ANTLR start "T__49"
    public final void mT__49() throws RecognitionException {
        try {
            int _type = T__49;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:36:7: ( '}' )
            // Lua.g:36:9: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__49"

    // $ANTLR start "T__50"
    public final void mT__50() throws RecognitionException {
        try {
            int _type = T__50;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:37:7: ( '+' )
            // Lua.g:37:9: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__50"

    // $ANTLR start "T__51"
    public final void mT__51() throws RecognitionException {
        try {
            int _type = T__51;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:38:7: ( '-' )
            // Lua.g:38:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__51"

    // $ANTLR start "T__52"
    public final void mT__52() throws RecognitionException {
        try {
            int _type = T__52;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:39:7: ( '*' )
            // Lua.g:39:9: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__52"

    // $ANTLR start "T__53"
    public final void mT__53() throws RecognitionException {
        try {
            int _type = T__53;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:40:7: ( '/' )
            // Lua.g:40:9: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__53"

    // $ANTLR start "T__54"
    public final void mT__54() throws RecognitionException {
        try {
            int _type = T__54;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:41:7: ( '^' )
            // Lua.g:41:9: '^'
            {
            match('^'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__54"

    // $ANTLR start "T__55"
    public final void mT__55() throws RecognitionException {
        try {
            int _type = T__55;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:42:7: ( '%' )
            // Lua.g:42:9: '%'
            {
            match('%'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__55"

    // $ANTLR start "T__56"
    public final void mT__56() throws RecognitionException {
        try {
            int _type = T__56;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:43:7: ( '..' )
            // Lua.g:43:9: '..'
            {
            match(".."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__56"

    // $ANTLR start "T__57"
    public final void mT__57() throws RecognitionException {
        try {
            int _type = T__57;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:44:7: ( '<' )
            // Lua.g:44:9: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__57"

    // $ANTLR start "T__58"
    public final void mT__58() throws RecognitionException {
        try {
            int _type = T__58;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:45:7: ( '<=' )
            // Lua.g:45:9: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__58"

    // $ANTLR start "T__59"
    public final void mT__59() throws RecognitionException {
        try {
            int _type = T__59;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:46:7: ( '>' )
            // Lua.g:46:9: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__59"

    // $ANTLR start "T__60"
    public final void mT__60() throws RecognitionException {
        try {
            int _type = T__60;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:47:7: ( '>=' )
            // Lua.g:47:9: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__60"

    // $ANTLR start "T__61"
    public final void mT__61() throws RecognitionException {
        try {
            int _type = T__61;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:48:7: ( '==' )
            // Lua.g:48:9: '=='
            {
            match("=="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__61"

    // $ANTLR start "T__62"
    public final void mT__62() throws RecognitionException {
        try {
            int _type = T__62;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:49:7: ( '~=' )
            // Lua.g:49:9: '~='
            {
            match("~="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__62"

    // $ANTLR start "T__63"
    public final void mT__63() throws RecognitionException {
        try {
            int _type = T__63;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:50:7: ( 'and' )
            // Lua.g:50:9: 'and'
            {
            match("and"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__63"

    // $ANTLR start "T__64"
    public final void mT__64() throws RecognitionException {
        try {
            int _type = T__64;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:51:7: ( 'or' )
            // Lua.g:51:9: 'or'
            {
            match("or"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__64"

    // $ANTLR start "T__65"
    public final void mT__65() throws RecognitionException {
        try {
            int _type = T__65;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:52:7: ( 'not' )
            // Lua.g:52:9: 'not'
            {
            match("not"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__65"

    // $ANTLR start "T__66"
    public final void mT__66() throws RecognitionException {
        try {
            int _type = T__66;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:53:7: ( '#' )
            // Lua.g:53:9: '#'
            {
            match('#'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__66"

    // $ANTLR start "NAME"
    public final void mNAME() throws RecognitionException {
        try {
            int _type = NAME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:240:6: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( options {greedy=true; } : 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )* )
            // Lua.g:240:7: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( options {greedy=true; } : 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // Lua.g:240:30: ( options {greedy=true; } : 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            loop1:
            do {
                int alt1=5;
                switch ( input.LA(1) ) {
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    {
                    alt1=1;
                    }
                    break;
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                    {
                    alt1=2;
                    }
                    break;
                case '_':
                    {
                    alt1=3;
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    {
                    alt1=4;
                    }
                    break;

                }

                switch (alt1) {
            	case 1 :
            	    // Lua.g:240:54: 'a' .. 'z'
            	    {
            	    matchRange('a','z'); 

            	    }
            	    break;
            	case 2 :
            	    // Lua.g:240:63: 'A' .. 'Z'
            	    {
            	    matchRange('A','Z'); 

            	    }
            	    break;
            	case 3 :
            	    // Lua.g:240:72: '_'
            	    {
            	    match('_'); 

            	    }
            	    break;
            	case 4 :
            	    // Lua.g:240:76: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NAME"

    // $ANTLR start "INT"
    public final void mINT() throws RecognitionException {
        try {
            int _type = INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:243:5: ( ( '0' .. '9' )+ )
            // Lua.g:243:7: ( '0' .. '9' )+
            {
            // Lua.g:243:7: ( '0' .. '9' )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='0' && LA2_0<='9')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // Lua.g:243:8: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INT"

    // $ANTLR start "FLOAT1"
    public final void mFLOAT1() throws RecognitionException {
        try {
            int _type = FLOAT1;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:245:9: ( '.' INT )
            // Lua.g:245:10: '.' INT
            {
            match('.'); 
            mINT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FLOAT1"

    // $ANTLR start "FLOAT2"
    public final void mFLOAT2() throws RecognitionException {
        try {
            int _type = FLOAT2;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:247:9: ( INT '.' )
            // Lua.g:247:10: INT '.'
            {
            mINT(); 
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FLOAT2"

    // $ANTLR start "FLOAT3"
    public final void mFLOAT3() throws RecognitionException {
        try {
            int _type = FLOAT3;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:249:9: ( INT '.' INT )
            // Lua.g:249:10: INT '.' INT
            {
            mINT(); 
            match('.'); 
            mINT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FLOAT3"

    // $ANTLR start "EXP"
    public final void mEXP() throws RecognitionException {
        try {
            int _type = EXP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:251:5: ( ( INT | FLOAT1 | FLOAT2 | FLOAT3 ) ( 'E' | 'e' ) ( '-' | '+' )? INT )
            // Lua.g:251:7: ( INT | FLOAT1 | FLOAT2 | FLOAT3 ) ( 'E' | 'e' ) ( '-' | '+' )? INT
            {
            // Lua.g:251:7: ( INT | FLOAT1 | FLOAT2 | FLOAT3 )
            int alt3=4;
            alt3 = dfa3.predict(input);
            switch (alt3) {
                case 1 :
                    // Lua.g:251:8: INT
                    {
                    mINT(); 

                    }
                    break;
                case 2 :
                    // Lua.g:251:14: FLOAT1
                    {
                    mFLOAT1(); 

                    }
                    break;
                case 3 :
                    // Lua.g:251:23: FLOAT2
                    {
                    mFLOAT2(); 

                    }
                    break;
                case 4 :
                    // Lua.g:251:32: FLOAT3
                    {
                    mFLOAT3(); 

                    }
                    break;

            }

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // Lua.g:251:50: ( '-' | '+' )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0=='+'||LA4_0=='-') ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // Lua.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            mINT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EXP"

    // $ANTLR start "HEX"
    public final void mHEX() throws RecognitionException {
        try {
            int _type = HEX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:253:5: ( '0' ( 'x' | 'X' ) ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )+ )
            // Lua.g:253:6: '0' ( 'x' | 'X' ) ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )+
            {
            match('0'); 
            if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // Lua.g:253:22: ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>='0' && LA5_0<='9')||(LA5_0>='A' && LA5_0<='F')||(LA5_0>='a' && LA5_0<='f')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // Lua.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt5 >= 1 ) break loop5;
                        EarlyExitException eee =
                            new EarlyExitException(5, input);
                        throw eee;
                }
                cnt5++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "HEX"

    // $ANTLR start "NORMALSTRING"
    public final void mNORMALSTRING() throws RecognitionException {
        try {
            int _type = NORMALSTRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:258:5: ( '\"' ( EscapeSequence | ~ ( '\\\\' | '\"' ) )* '\"' )
            // Lua.g:258:8: '\"' ( EscapeSequence | ~ ( '\\\\' | '\"' ) )* '\"'
            {
            match('\"'); 
            // Lua.g:258:12: ( EscapeSequence | ~ ( '\\\\' | '\"' ) )*
            loop6:
            do {
                int alt6=3;
                int LA6_0 = input.LA(1);

                if ( (LA6_0=='\\') ) {
                    alt6=1;
                }
                else if ( ((LA6_0>='\u0000' && LA6_0<='!')||(LA6_0>='#' && LA6_0<='[')||(LA6_0>=']' && LA6_0<='\uFFFF')) ) {
                    alt6=2;
                }


                switch (alt6) {
            	case 1 :
            	    // Lua.g:258:14: EscapeSequence
            	    {
            	    mEscapeSequence(); 

            	    }
            	    break;
            	case 2 :
            	    // Lua.g:258:31: ~ ( '\\\\' | '\"' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NORMALSTRING"

    // $ANTLR start "CHARSTRING"
    public final void mCHARSTRING() throws RecognitionException {
        try {
            int _type = CHARSTRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:262:4: ( '\\'' ( EscapeSequence | ~ ( '\\\\' | '\\'' ) )* '\\'' )
            // Lua.g:262:6: '\\'' ( EscapeSequence | ~ ( '\\\\' | '\\'' ) )* '\\''
            {
            match('\''); 
            // Lua.g:262:11: ( EscapeSequence | ~ ( '\\\\' | '\\'' ) )*
            loop7:
            do {
                int alt7=3;
                int LA7_0 = input.LA(1);

                if ( (LA7_0=='\\') ) {
                    alt7=1;
                }
                else if ( ((LA7_0>='\u0000' && LA7_0<='&')||(LA7_0>='(' && LA7_0<='[')||(LA7_0>=']' && LA7_0<='\uFFFF')) ) {
                    alt7=2;
                }


                switch (alt7) {
            	case 1 :
            	    // Lua.g:262:13: EscapeSequence
            	    {
            	    mEscapeSequence(); 

            	    }
            	    break;
            	case 2 :
            	    // Lua.g:262:30: ~ ( '\\\\' | '\\'' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);

            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CHARSTRING"

    // $ANTLR start "LONGSTRING"
    public final void mLONGSTRING() throws RecognitionException {
        try {
            int _type = LONGSTRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:266:2: ( '[' ( '=' )* '[' ( EscapeSequence | ~ ( '\\\\' | ']' ) )* ']' ( '=' )* ']' )
            // Lua.g:266:4: '[' ( '=' )* '[' ( EscapeSequence | ~ ( '\\\\' | ']' ) )* ']' ( '=' )* ']'
            {
            match('['); 
            // Lua.g:266:7: ( '=' )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0=='=') ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // Lua.g:266:8: '='
            	    {
            	    match('='); 

            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

            match('['); 
            // Lua.g:266:17: ( EscapeSequence | ~ ( '\\\\' | ']' ) )*
            loop9:
            do {
                int alt9=3;
                int LA9_0 = input.LA(1);

                if ( (LA9_0=='\\') ) {
                    alt9=1;
                }
                else if ( ((LA9_0>='\u0000' && LA9_0<='[')||(LA9_0>='^' && LA9_0<='\uFFFF')) ) {
                    alt9=2;
                }


                switch (alt9) {
            	case 1 :
            	    // Lua.g:266:19: EscapeSequence
            	    {
            	    mEscapeSequence(); 

            	    }
            	    break;
            	case 2 :
            	    // Lua.g:266:36: ~ ( '\\\\' | ']' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='[')||(input.LA(1)>='^' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            match(']'); 
            // Lua.g:266:54: ( '=' )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0=='=') ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // Lua.g:266:55: '='
            	    {
            	    match('='); 

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);

            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LONGSTRING"

    // $ANTLR start "EscapeSequence"
    public final void mEscapeSequence() throws RecognitionException {
        try {
            // Lua.g:271:5: ( '\\\\' ( 'a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' | '\\\"' | '\\'' | '\\\\' | '\\n' ) | DecimalEscape )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0=='\\') ) {
                int LA11_1 = input.LA(2);

                if ( (LA11_1=='\n'||LA11_1=='\"'||LA11_1=='\''||LA11_1=='\\'||(LA11_1>='a' && LA11_1<='b')||LA11_1=='f'||LA11_1=='n'||LA11_1=='r'||LA11_1=='t'||LA11_1=='v') ) {
                    alt11=1;
                }
                else if ( ((LA11_1>='0' && LA11_1<='9')) ) {
                    alt11=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 11, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // Lua.g:271:9: '\\\\' ( 'a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' | '\\\"' | '\\'' | '\\\\' | '\\n' )
                    {
                    match('\\'); 
                    if ( input.LA(1)=='\n'||input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||(input.LA(1)>='a' && input.LA(1)<='b')||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t'||input.LA(1)=='v' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // Lua.g:272:9: DecimalEscape
                    {
                    mDecimalEscape(); 

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "EscapeSequence"

    // $ANTLR start "DecimalEscape"
    public final void mDecimalEscape() throws RecognitionException {
        try {
            // Lua.g:277:5: ( '\\\\' ( '0' .. '9' ) ( ( '0' .. '9' ) ( '0' .. '9' )? )? )
            // Lua.g:277:9: '\\\\' ( '0' .. '9' ) ( ( '0' .. '9' ) ( '0' .. '9' )? )?
            {
            match('\\'); 
            // Lua.g:277:14: ( '0' .. '9' )
            // Lua.g:277:15: '0' .. '9'
            {
            matchRange('0','9'); 

            }

            // Lua.g:277:25: ( ( '0' .. '9' ) ( '0' .. '9' )? )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( ((LA13_0>='0' && LA13_0<='9')) ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // Lua.g:277:26: ( '0' .. '9' ) ( '0' .. '9' )?
                    {
                    // Lua.g:277:26: ( '0' .. '9' )
                    // Lua.g:277:27: '0' .. '9'
                    {
                    matchRange('0','9'); 

                    }

                    // Lua.g:277:37: ( '0' .. '9' )?
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( ((LA12_0>='0' && LA12_0<='9')) ) {
                        alt12=1;
                    }
                    switch (alt12) {
                        case 1 :
                            // Lua.g:277:38: '0' .. '9'
                            {
                            matchRange('0','9'); 

                            }
                            break;

                    }


                    }
                    break;

            }


            }

        }
        finally {
        }
    }
    // $ANTLR end "DecimalEscape"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:282:5: ( '--[[' ( options {greedy=false; } : . )* '--]]' | '--[=[' ( options {greedy=false; } : . )* '--]==]' | '--[==[' ( options {greedy=false; } : . )* '--]==]' | '--[===[' ( options {greedy=false; } : . )* '--]===]' )
            int alt18=4;
            alt18 = dfa18.predict(input);
            switch (alt18) {
                case 1 :
                    // Lua.g:282:9: '--[[' ( options {greedy=false; } : . )* '--]]'
                    {
                    match("--[["); 

                    // Lua.g:282:16: ( options {greedy=false; } : . )*
                    loop14:
                    do {
                        int alt14=2;
                        int LA14_0 = input.LA(1);

                        if ( (LA14_0=='-') ) {
                            int LA14_1 = input.LA(2);

                            if ( (LA14_1=='-') ) {
                                int LA14_3 = input.LA(3);

                                if ( (LA14_3==']') ) {
                                    int LA14_4 = input.LA(4);

                                    if ( (LA14_4==']') ) {
                                        alt14=2;
                                    }
                                    else if ( ((LA14_4>='\u0000' && LA14_4<='\\')||(LA14_4>='^' && LA14_4<='\uFFFF')) ) {
                                        alt14=1;
                                    }


                                }
                                else if ( ((LA14_3>='\u0000' && LA14_3<='\\')||(LA14_3>='^' && LA14_3<='\uFFFF')) ) {
                                    alt14=1;
                                }


                            }
                            else if ( ((LA14_1>='\u0000' && LA14_1<=',')||(LA14_1>='.' && LA14_1<='\uFFFF')) ) {
                                alt14=1;
                            }


                        }
                        else if ( ((LA14_0>='\u0000' && LA14_0<=',')||(LA14_0>='.' && LA14_0<='\uFFFF')) ) {
                            alt14=1;
                        }


                        switch (alt14) {
                    	case 1 :
                    	    // Lua.g:282:44: .
                    	    {
                    	    matchAny(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop14;
                        }
                    } while (true);

                    match("--]]"); 

                    skip();

                    }
                    break;
                case 2 :
                    // Lua.g:283:9: '--[=[' ( options {greedy=false; } : . )* '--]==]'
                    {
                    match("--[=["); 

                    // Lua.g:283:17: ( options {greedy=false; } : . )*
                    loop15:
                    do {
                        int alt15=2;
                        int LA15_0 = input.LA(1);

                        if ( (LA15_0=='-') ) {
                            int LA15_1 = input.LA(2);

                            if ( (LA15_1=='-') ) {
                                int LA15_3 = input.LA(3);

                                if ( (LA15_3==']') ) {
                                    int LA15_4 = input.LA(4);

                                    if ( (LA15_4=='=') ) {
                                        int LA15_5 = input.LA(5);

                                        if ( (LA15_5=='=') ) {
                                            int LA15_6 = input.LA(6);

                                            if ( (LA15_6==']') ) {
                                                alt15=2;
                                            }
                                            else if ( ((LA15_6>='\u0000' && LA15_6<='\\')||(LA15_6>='^' && LA15_6<='\uFFFF')) ) {
                                                alt15=1;
                                            }


                                        }
                                        else if ( ((LA15_5>='\u0000' && LA15_5<='<')||(LA15_5>='>' && LA15_5<='\uFFFF')) ) {
                                            alt15=1;
                                        }


                                    }
                                    else if ( ((LA15_4>='\u0000' && LA15_4<='<')||(LA15_4>='>' && LA15_4<='\uFFFF')) ) {
                                        alt15=1;
                                    }


                                }
                                else if ( ((LA15_3>='\u0000' && LA15_3<='\\')||(LA15_3>='^' && LA15_3<='\uFFFF')) ) {
                                    alt15=1;
                                }


                            }
                            else if ( ((LA15_1>='\u0000' && LA15_1<=',')||(LA15_1>='.' && LA15_1<='\uFFFF')) ) {
                                alt15=1;
                            }


                        }
                        else if ( ((LA15_0>='\u0000' && LA15_0<=',')||(LA15_0>='.' && LA15_0<='\uFFFF')) ) {
                            alt15=1;
                        }


                        switch (alt15) {
                    	case 1 :
                    	    // Lua.g:283:45: .
                    	    {
                    	    matchAny(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop15;
                        }
                    } while (true);

                    match("--]==]"); 

                    skip();

                    }
                    break;
                case 3 :
                    // Lua.g:284:9: '--[==[' ( options {greedy=false; } : . )* '--]==]'
                    {
                    match("--[==["); 

                    // Lua.g:284:18: ( options {greedy=false; } : . )*
                    loop16:
                    do {
                        int alt16=2;
                        int LA16_0 = input.LA(1);

                        if ( (LA16_0=='-') ) {
                            int LA16_1 = input.LA(2);

                            if ( (LA16_1=='-') ) {
                                int LA16_3 = input.LA(3);

                                if ( (LA16_3==']') ) {
                                    int LA16_4 = input.LA(4);

                                    if ( (LA16_4=='=') ) {
                                        int LA16_5 = input.LA(5);

                                        if ( (LA16_5=='=') ) {
                                            int LA16_6 = input.LA(6);

                                            if ( (LA16_6==']') ) {
                                                alt16=2;
                                            }
                                            else if ( ((LA16_6>='\u0000' && LA16_6<='\\')||(LA16_6>='^' && LA16_6<='\uFFFF')) ) {
                                                alt16=1;
                                            }


                                        }
                                        else if ( ((LA16_5>='\u0000' && LA16_5<='<')||(LA16_5>='>' && LA16_5<='\uFFFF')) ) {
                                            alt16=1;
                                        }


                                    }
                                    else if ( ((LA16_4>='\u0000' && LA16_4<='<')||(LA16_4>='>' && LA16_4<='\uFFFF')) ) {
                                        alt16=1;
                                    }


                                }
                                else if ( ((LA16_3>='\u0000' && LA16_3<='\\')||(LA16_3>='^' && LA16_3<='\uFFFF')) ) {
                                    alt16=1;
                                }


                            }
                            else if ( ((LA16_1>='\u0000' && LA16_1<=',')||(LA16_1>='.' && LA16_1<='\uFFFF')) ) {
                                alt16=1;
                            }


                        }
                        else if ( ((LA16_0>='\u0000' && LA16_0<=',')||(LA16_0>='.' && LA16_0<='\uFFFF')) ) {
                            alt16=1;
                        }


                        switch (alt16) {
                    	case 1 :
                    	    // Lua.g:284:46: .
                    	    {
                    	    matchAny(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop16;
                        }
                    } while (true);

                    match("--]==]"); 

                    skip();

                    }
                    break;
                case 4 :
                    // Lua.g:285:9: '--[===[' ( options {greedy=false; } : . )* '--]===]'
                    {
                    match("--[===["); 

                    // Lua.g:285:19: ( options {greedy=false; } : . )*
                    loop17:
                    do {
                        int alt17=2;
                        int LA17_0 = input.LA(1);

                        if ( (LA17_0=='-') ) {
                            int LA17_1 = input.LA(2);

                            if ( (LA17_1=='-') ) {
                                int LA17_3 = input.LA(3);

                                if ( (LA17_3==']') ) {
                                    int LA17_4 = input.LA(4);

                                    if ( (LA17_4=='=') ) {
                                        int LA17_5 = input.LA(5);

                                        if ( (LA17_5=='=') ) {
                                            int LA17_6 = input.LA(6);

                                            if ( (LA17_6=='=') ) {
                                                int LA17_7 = input.LA(7);

                                                if ( (LA17_7==']') ) {
                                                    alt17=2;
                                                }
                                                else if ( ((LA17_7>='\u0000' && LA17_7<='\\')||(LA17_7>='^' && LA17_7<='\uFFFF')) ) {
                                                    alt17=1;
                                                }


                                            }
                                            else if ( ((LA17_6>='\u0000' && LA17_6<='<')||(LA17_6>='>' && LA17_6<='\uFFFF')) ) {
                                                alt17=1;
                                            }


                                        }
                                        else if ( ((LA17_5>='\u0000' && LA17_5<='<')||(LA17_5>='>' && LA17_5<='\uFFFF')) ) {
                                            alt17=1;
                                        }


                                    }
                                    else if ( ((LA17_4>='\u0000' && LA17_4<='<')||(LA17_4>='>' && LA17_4<='\uFFFF')) ) {
                                        alt17=1;
                                    }


                                }
                                else if ( ((LA17_3>='\u0000' && LA17_3<='\\')||(LA17_3>='^' && LA17_3<='\uFFFF')) ) {
                                    alt17=1;
                                }


                            }
                            else if ( ((LA17_1>='\u0000' && LA17_1<=',')||(LA17_1>='.' && LA17_1<='\uFFFF')) ) {
                                alt17=1;
                            }


                        }
                        else if ( ((LA17_0>='\u0000' && LA17_0<=',')||(LA17_0>='.' && LA17_0<='\uFFFF')) ) {
                            alt17=1;
                        }


                        switch (alt17) {
                    	case 1 :
                    	    // Lua.g:285:47: .
                    	    {
                    	    matchAny(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop17;
                        }
                    } while (true);

                    match("--]===]"); 

                    skip();

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMENT"

    // $ANTLR start "LINE_COMMENT"
    public final void mLINE_COMMENT() throws RecognitionException {
        try {
            int _type = LINE_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:289:5: ( '--' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n' )
            // Lua.g:289:7: '--' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n'
            {
            match("--"); 

            // Lua.g:289:12: (~ ( '\\n' | '\\r' ) )*
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( ((LA19_0>='\u0000' && LA19_0<='\t')||(LA19_0>='\u000B' && LA19_0<='\f')||(LA19_0>='\u000E' && LA19_0<='\uFFFF')) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // Lua.g:289:12: ~ ( '\\n' | '\\r' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop19;
                }
            } while (true);

            // Lua.g:289:26: ( '\\r' )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0=='\r') ) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // Lua.g:289:26: '\\r'
                    {
                    match('\r'); 

                    }
                    break;

            }

            match('\n'); 
            skip();

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LINE_COMMENT"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:293:5: ( ( ' ' | '\\t' | '\\u000C' ) )
            // Lua.g:293:8: ( ' ' | '\\t' | '\\u000C' )
            {
            if ( input.LA(1)=='\t'||input.LA(1)=='\f'||input.LA(1)==' ' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            skip();

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "NEWLINE"
    public final void mNEWLINE() throws RecognitionException {
        try {
            int _type = NEWLINE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Lua.g:296:9: ( ( '\\r' )? '\\n' )
            // Lua.g:296:11: ( '\\r' )? '\\n'
            {
            // Lua.g:296:11: ( '\\r' )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0=='\r') ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // Lua.g:296:12: '\\r'
                    {
                    match('\r'); 

                    }
                    break;

            }

            match('\n'); 
            skip();

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NEWLINE"

    public void mTokens() throws RecognitionException {
        // Lua.g:1:8: ( T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | T__56 | T__57 | T__58 | T__59 | T__60 | T__61 | T__62 | T__63 | T__64 | T__65 | T__66 | NAME | INT | FLOAT1 | FLOAT2 | FLOAT3 | EXP | HEX | NORMALSTRING | CHARSTRING | LONGSTRING | COMMENT | LINE_COMMENT | WS | NEWLINE )
        int alt22=61;
        alt22 = dfa22.predict(input);
        switch (alt22) {
            case 1 :
                // Lua.g:1:10: T__20
                {
                mT__20(); 

                }
                break;
            case 2 :
                // Lua.g:1:16: T__21
                {
                mT__21(); 

                }
                break;
            case 3 :
                // Lua.g:1:22: T__22
                {
                mT__22(); 

                }
                break;
            case 4 :
                // Lua.g:1:28: T__23
                {
                mT__23(); 

                }
                break;
            case 5 :
                // Lua.g:1:34: T__24
                {
                mT__24(); 

                }
                break;
            case 6 :
                // Lua.g:1:40: T__25
                {
                mT__25(); 

                }
                break;
            case 7 :
                // Lua.g:1:46: T__26
                {
                mT__26(); 

                }
                break;
            case 8 :
                // Lua.g:1:52: T__27
                {
                mT__27(); 

                }
                break;
            case 9 :
                // Lua.g:1:58: T__28
                {
                mT__28(); 

                }
                break;
            case 10 :
                // Lua.g:1:64: T__29
                {
                mT__29(); 

                }
                break;
            case 11 :
                // Lua.g:1:70: T__30
                {
                mT__30(); 

                }
                break;
            case 12 :
                // Lua.g:1:76: T__31
                {
                mT__31(); 

                }
                break;
            case 13 :
                // Lua.g:1:82: T__32
                {
                mT__32(); 

                }
                break;
            case 14 :
                // Lua.g:1:88: T__33
                {
                mT__33(); 

                }
                break;
            case 15 :
                // Lua.g:1:94: T__34
                {
                mT__34(); 

                }
                break;
            case 16 :
                // Lua.g:1:100: T__35
                {
                mT__35(); 

                }
                break;
            case 17 :
                // Lua.g:1:106: T__36
                {
                mT__36(); 

                }
                break;
            case 18 :
                // Lua.g:1:112: T__37
                {
                mT__37(); 

                }
                break;
            case 19 :
                // Lua.g:1:118: T__38
                {
                mT__38(); 

                }
                break;
            case 20 :
                // Lua.g:1:124: T__39
                {
                mT__39(); 

                }
                break;
            case 21 :
                // Lua.g:1:130: T__40
                {
                mT__40(); 

                }
                break;
            case 22 :
                // Lua.g:1:136: T__41
                {
                mT__41(); 

                }
                break;
            case 23 :
                // Lua.g:1:142: T__42
                {
                mT__42(); 

                }
                break;
            case 24 :
                // Lua.g:1:148: T__43
                {
                mT__43(); 

                }
                break;
            case 25 :
                // Lua.g:1:154: T__44
                {
                mT__44(); 

                }
                break;
            case 26 :
                // Lua.g:1:160: T__45
                {
                mT__45(); 

                }
                break;
            case 27 :
                // Lua.g:1:166: T__46
                {
                mT__46(); 

                }
                break;
            case 28 :
                // Lua.g:1:172: T__47
                {
                mT__47(); 

                }
                break;
            case 29 :
                // Lua.g:1:178: T__48
                {
                mT__48(); 

                }
                break;
            case 30 :
                // Lua.g:1:184: T__49
                {
                mT__49(); 

                }
                break;
            case 31 :
                // Lua.g:1:190: T__50
                {
                mT__50(); 

                }
                break;
            case 32 :
                // Lua.g:1:196: T__51
                {
                mT__51(); 

                }
                break;
            case 33 :
                // Lua.g:1:202: T__52
                {
                mT__52(); 

                }
                break;
            case 34 :
                // Lua.g:1:208: T__53
                {
                mT__53(); 

                }
                break;
            case 35 :
                // Lua.g:1:214: T__54
                {
                mT__54(); 

                }
                break;
            case 36 :
                // Lua.g:1:220: T__55
                {
                mT__55(); 

                }
                break;
            case 37 :
                // Lua.g:1:226: T__56
                {
                mT__56(); 

                }
                break;
            case 38 :
                // Lua.g:1:232: T__57
                {
                mT__57(); 

                }
                break;
            case 39 :
                // Lua.g:1:238: T__58
                {
                mT__58(); 

                }
                break;
            case 40 :
                // Lua.g:1:244: T__59
                {
                mT__59(); 

                }
                break;
            case 41 :
                // Lua.g:1:250: T__60
                {
                mT__60(); 

                }
                break;
            case 42 :
                // Lua.g:1:256: T__61
                {
                mT__61(); 

                }
                break;
            case 43 :
                // Lua.g:1:262: T__62
                {
                mT__62(); 

                }
                break;
            case 44 :
                // Lua.g:1:268: T__63
                {
                mT__63(); 

                }
                break;
            case 45 :
                // Lua.g:1:274: T__64
                {
                mT__64(); 

                }
                break;
            case 46 :
                // Lua.g:1:280: T__65
                {
                mT__65(); 

                }
                break;
            case 47 :
                // Lua.g:1:286: T__66
                {
                mT__66(); 

                }
                break;
            case 48 :
                // Lua.g:1:292: NAME
                {
                mNAME(); 

                }
                break;
            case 49 :
                // Lua.g:1:297: INT
                {
                mINT(); 

                }
                break;
            case 50 :
                // Lua.g:1:301: FLOAT1
                {
                mFLOAT1(); 

                }
                break;
            case 51 :
                // Lua.g:1:308: FLOAT2
                {
                mFLOAT2(); 

                }
                break;
            case 52 :
                // Lua.g:1:315: FLOAT3
                {
                mFLOAT3(); 

                }
                break;
            case 53 :
                // Lua.g:1:322: EXP
                {
                mEXP(); 

                }
                break;
            case 54 :
                // Lua.g:1:326: HEX
                {
                mHEX(); 

                }
                break;
            case 55 :
                // Lua.g:1:330: NORMALSTRING
                {
                mNORMALSTRING(); 

                }
                break;
            case 56 :
                // Lua.g:1:343: CHARSTRING
                {
                mCHARSTRING(); 

                }
                break;
            case 57 :
                // Lua.g:1:354: LONGSTRING
                {
                mLONGSTRING(); 

                }
                break;
            case 58 :
                // Lua.g:1:365: COMMENT
                {
                mCOMMENT(); 

                }
                break;
            case 59 :
                // Lua.g:1:373: LINE_COMMENT
                {
                mLINE_COMMENT(); 

                }
                break;
            case 60 :
                // Lua.g:1:386: WS
                {
                mWS(); 

                }
                break;
            case 61 :
                // Lua.g:1:389: NEWLINE
                {
                mNEWLINE(); 

                }
                break;

        }

    }


    protected DFA3 dfa3 = new DFA3(this);
    protected DFA18 dfa18 = new DFA18(this);
    protected DFA22 dfa22 = new DFA22(this);
    static final String DFA3_eotS =
        "\7\uffff";
    static final String DFA3_eofS =
        "\7\uffff";
    static final String DFA3_minS =
        "\2\56\2\uffff\1\60\2\uffff";
    static final String DFA3_maxS =
        "\1\71\1\145\2\uffff\1\145\2\uffff";
    static final String DFA3_acceptS =
        "\2\uffff\1\2\1\1\1\uffff\1\4\1\3";
    static final String DFA3_specialS =
        "\7\uffff}>";
    static final String[] DFA3_transitionS = {
            "\1\2\1\uffff\12\1",
            "\1\4\1\uffff\12\1\13\uffff\1\3\37\uffff\1\3",
            "",
            "",
            "\12\5\13\uffff\1\6\37\uffff\1\6",
            "",
            ""
    };

    static final short[] DFA3_eot = DFA.unpackEncodedString(DFA3_eotS);
    static final short[] DFA3_eof = DFA.unpackEncodedString(DFA3_eofS);
    static final char[] DFA3_min = DFA.unpackEncodedStringToUnsignedChars(DFA3_minS);
    static final char[] DFA3_max = DFA.unpackEncodedStringToUnsignedChars(DFA3_maxS);
    static final short[] DFA3_accept = DFA.unpackEncodedString(DFA3_acceptS);
    static final short[] DFA3_special = DFA.unpackEncodedString(DFA3_specialS);
    static final short[][] DFA3_transition;

    static {
        int numStates = DFA3_transitionS.length;
        DFA3_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA3_transition[i] = DFA.unpackEncodedString(DFA3_transitionS[i]);
        }
    }

    class DFA3 extends DFA {

        public DFA3(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 3;
            this.eot = DFA3_eot;
            this.eof = DFA3_eof;
            this.min = DFA3_min;
            this.max = DFA3_max;
            this.accept = DFA3_accept;
            this.special = DFA3_special;
            this.transition = DFA3_transition;
        }
        public String getDescription() {
            return "251:7: ( INT | FLOAT1 | FLOAT2 | FLOAT3 )";
        }
    }
    static final String DFA18_eotS =
        "\12\uffff";
    static final String DFA18_eofS =
        "\12\uffff";
    static final String DFA18_minS =
        "\2\55\1\133\1\75\1\uffff\1\75\1\uffff\1\75\2\uffff";
    static final String DFA18_maxS =
        "\2\55\2\133\1\uffff\1\133\1\uffff\1\133\2\uffff";
    static final String DFA18_acceptS =
        "\4\uffff\1\1\1\uffff\1\2\1\uffff\1\3\1\4";
    static final String DFA18_specialS =
        "\12\uffff}>";
    static final String[] DFA18_transitionS = {
            "\1\1",
            "\1\2",
            "\1\3",
            "\1\5\35\uffff\1\4",
            "",
            "\1\7\35\uffff\1\6",
            "",
            "\1\11\35\uffff\1\10",
            "",
            ""
    };

    static final short[] DFA18_eot = DFA.unpackEncodedString(DFA18_eotS);
    static final short[] DFA18_eof = DFA.unpackEncodedString(DFA18_eofS);
    static final char[] DFA18_min = DFA.unpackEncodedStringToUnsignedChars(DFA18_minS);
    static final char[] DFA18_max = DFA.unpackEncodedStringToUnsignedChars(DFA18_maxS);
    static final short[] DFA18_accept = DFA.unpackEncodedString(DFA18_acceptS);
    static final short[] DFA18_special = DFA.unpackEncodedString(DFA18_specialS);
    static final short[][] DFA18_transition;

    static {
        int numStates = DFA18_transitionS.length;
        DFA18_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA18_transition[i] = DFA.unpackEncodedString(DFA18_transitionS[i]);
        }
    }

    class DFA18 extends DFA {

        public DFA18(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 18;
            this.eot = DFA18_eot;
            this.eof = DFA18_eof;
            this.min = DFA18_min;
            this.max = DFA18_max;
            this.accept = DFA18_accept;
            this.special = DFA18_special;
            this.transition = DFA18_transition;
        }
        public String getDescription() {
            return "281:1: COMMENT : ( '--[[' ( options {greedy=false; } : . )* '--]]' | '--[=[' ( options {greedy=false; } : . )* '--]==]' | '--[==[' ( options {greedy=false; } : . )* '--]==]' | '--[===[' ( options {greedy=false; } : . )* '--]===]' );";
        }
    }
    static final String DFA22_eotS =
        "\2\uffff\1\53\6\43\1\uffff\4\43\1\75\1\uffff\1\43\2\uffff\1\101"+
        "\4\uffff\1\103\4\uffff\1\105\1\107\1\uffff\2\43\2\uffff\2\113\6"+
        "\uffff\1\116\10\43\1\130\1\131\4\43\1\137\1\140\1\uffff\2\43\10"+
        "\uffff\1\43\1\146\2\uffff\1\150\2\uffff\1\151\5\43\1\157\2\43\2"+
        "\uffff\4\43\3\uffff\1\166\1\167\2\uffff\1\172\1\uffff\1\173\2\uffff"+
        "\1\175\4\43\1\uffff\3\43\1\u0085\1\u0086\1\43\6\uffff\1\43\1\uffff"+
        "\1\u008f\2\43\1\u0092\1\43\1\u0094\1\u0095\2\uffff\1\u0096\2\uffff"+
        "\1\144\3\uffff\1\u009f\1\uffff\1\u00a0\1\u00a1\1\uffff\1\43\7\uffff"+
        "\1\144\6\uffff\1\43\3\uffff\1\144\3\uffff\1\u00b2\1\u0098\3\uffff"+
        "\1\144\11\uffff\1\u0098\2\uffff\1\u0098\2\uffff\1\u0098";
    static final String DFA22_eofS =
        "\u00c0\uffff";
    static final String DFA22_minS =
        "\1\11\1\uffff\1\75\1\157\1\154\1\150\1\145\1\156\1\141\1\uffff"+
        "\1\146\1\157\1\150\1\162\1\56\1\uffff\1\151\2\uffff\1\75\4\uffff"+
        "\1\55\4\uffff\2\75\1\uffff\1\156\1\162\2\uffff\2\56\6\uffff\1\60"+
        "\1\144\1\163\1\151\1\160\1\164\1\162\1\156\1\154\2\60\1\143\1\145"+
        "\1\165\1\145\1\56\1\60\1\uffff\1\154\1\164\2\uffff\1\0\5\uffff\1"+
        "\144\1\60\2\uffff\1\60\2\uffff\1\60\1\145\1\154\1\145\1\165\1\151"+
        "\1\60\1\143\1\163\2\uffff\1\141\1\156\1\145\1\141\3\uffff\2\60\1"+
        "\0\1\uffff\1\60\1\uffff\1\60\2\uffff\1\60\1\145\1\141\1\162\1\154"+
        "\1\uffff\1\164\1\145\1\154\2\60\1\153\2\uffff\2\0\2\uffff\1\146"+
        "\1\uffff\1\60\1\164\1\156\1\60\1\151\2\60\2\uffff\1\60\6\0\1\60"+
        "\1\uffff\2\60\1\uffff\1\157\3\uffff\1\0\1\uffff\6\0\3\uffff\1\156"+
        "\7\0\1\60\7\0\1\uffff\15\0";
    static final String DFA22_maxS =
        "\1\176\1\uffff\1\75\1\157\1\156\1\150\1\145\1\156\1\165\1\uffff"+
        "\1\156\1\157\2\162\1\71\1\uffff\1\157\2\uffff\1\133\4\uffff\1\55"+
        "\4\uffff\2\75\1\uffff\1\156\1\162\2\uffff\1\170\1\145\6\uffff\1"+
        "\172\1\144\1\163\1\151\2\164\1\162\1\156\1\154\2\172\1\143\1\145"+
        "\1\165\1\145\1\56\1\145\1\uffff\1\154\1\164\2\uffff\1\uffff\5\uffff"+
        "\1\144\1\172\2\uffff\1\145\2\uffff\1\172\1\145\1\154\1\145\1\165"+
        "\1\151\1\172\1\143\1\163\2\uffff\1\141\1\156\1\145\1\141\3\uffff"+
        "\2\172\1\uffff\1\uffff\1\172\1\uffff\1\145\2\uffff\1\172\1\145\1"+
        "\141\1\162\1\154\1\uffff\1\164\1\145\1\154\2\172\1\153\2\uffff\2"+
        "\uffff\2\uffff\1\146\1\uffff\1\172\1\164\1\156\1\172\1\151\2\172"+
        "\2\uffff\1\172\6\uffff\1\172\1\uffff\2\172\1\uffff\1\157\3\uffff"+
        "\1\uffff\1\uffff\6\uffff\3\uffff\1\156\7\uffff\1\172\7\uffff\1\uffff"+
        "\15\uffff";
    static final String DFA22_acceptS =
        "\1\uffff\1\1\7\uffff\1\11\5\uffff\1\24\1\uffff\1\31\1\32\1\uffff"+
        "\1\34\1\35\1\36\1\37\1\uffff\1\41\1\42\1\43\1\44\2\uffff\1\53\2"+
        "\uffff\1\57\1\60\2\uffff\1\67\1\70\1\74\1\75\1\52\1\2\21\uffff\1"+
        "\23\2\uffff\1\71\1\33\1\uffff\1\40\1\47\1\46\1\51\1\50\2\uffff\1"+
        "\66\1\61\1\uffff\1\65\1\3\11\uffff\1\12\1\15\4\uffff\1\30\1\45\1"+
        "\62\3\uffff\1\73\1\uffff\1\55\1\uffff\1\63\1\4\5\uffff\1\10\6\uffff"+
        "\1\25\1\56\2\uffff\1\54\1\64\1\uffff\1\20\7\uffff\1\16\1\27\10\uffff"+
        "\1\5\2\uffff\1\7\1\uffff\1\26\1\14\1\22\1\uffff\1\72\6\uffff\1\17"+
        "\1\6\1\21\20\uffff\1\13\15\uffff";
    static final String DFA22_specialS =
        "\102\uffff\1\34\40\uffff\1\50\24\uffff\1\5\1\52\16\uffff\1\15\1"+
        "\26\1\42\1\11\1\22\1\2\11\uffff\1\0\1\uffff\1\12\1\16\1\33\1\53"+
        "\1\41\1\1\4\uffff\1\14\1\25\1\4\1\51\1\23\1\45\1\46\1\uffff\1\3"+
        "\1\6\1\35\1\47\1\13\1\30\1\27\1\uffff\1\10\1\24\1\43\1\7\1\21\1"+
        "\32\1\44\1\20\1\31\1\36\1\37\1\40\1\17}>";
    static final String[] DFA22_transitionS = {
            "\1\50\1\51\1\uffff\1\50\1\51\22\uffff\1\50\1\uffff\1\46\1\42"+
            "\1\uffff\1\34\1\uffff\1\47\1\21\1\22\1\31\1\27\1\11\1\30\1\16"+
            "\1\32\1\44\11\45\1\17\1\1\1\35\1\2\1\36\2\uffff\32\43\1\23\1"+
            "\uffff\1\24\1\33\1\43\1\uffff\1\40\1\15\1\43\1\3\1\4\1\10\2"+
            "\43\1\12\2\43\1\13\1\43\1\20\1\41\2\43\1\6\1\43\1\14\1\7\1\43"+
            "\1\5\3\43\1\25\1\uffff\1\26\1\37",
            "",
            "\1\52",
            "\1\54",
            "\1\56\1\uffff\1\55",
            "\1\57",
            "\1\60",
            "\1\61",
            "\1\64\15\uffff\1\62\5\uffff\1\63",
            "",
            "\1\66\7\uffff\1\65",
            "\1\67",
            "\1\70\11\uffff\1\71",
            "\1\72",
            "\1\73\1\uffff\12\74",
            "",
            "\1\76\5\uffff\1\77",
            "",
            "",
            "\1\100\35\uffff\1\100",
            "",
            "",
            "",
            "",
            "\1\102",
            "",
            "",
            "",
            "",
            "\1\104",
            "\1\106",
            "",
            "\1\110",
            "\1\111",
            "",
            "",
            "\1\114\1\uffff\12\45\13\uffff\1\115\22\uffff\1\112\14\uffff"+
            "\1\115\22\uffff\1\112",
            "\1\114\1\uffff\12\45\13\uffff\1\115\37\uffff\1\115",
            "",
            "",
            "",
            "",
            "",
            "",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "\1\117",
            "\1\120",
            "\1\121",
            "\1\122\3\uffff\1\123",
            "\1\124",
            "\1\125",
            "\1\126",
            "\1\127",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "\1\132",
            "\1\133",
            "\1\134",
            "\1\135",
            "\1\136",
            "\12\74\13\uffff\1\115\37\uffff\1\115",
            "",
            "\1\141",
            "\1\142",
            "",
            "",
            "\133\144\1\143\uffa4\144",
            "",
            "",
            "",
            "",
            "",
            "\1\145",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "",
            "",
            "\12\147\13\uffff\1\115\37\uffff\1\115",
            "",
            "",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "\1\152",
            "\1\153",
            "\1\154",
            "\1\155",
            "\1\156",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "\1\160",
            "\1\161",
            "",
            "",
            "\1\162",
            "\1\163",
            "\1\164",
            "\1\165",
            "",
            "",
            "",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "\75\144\1\171\35\144\1\170\uffa4\144",
            "",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "",
            "\12\147\13\uffff\1\115\37\uffff\1\115",
            "",
            "",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\10\43\1\174\21\43",
            "\1\176",
            "\1\177",
            "\1\u0080",
            "\1\u0081",
            "",
            "\1\u0082",
            "\1\u0083",
            "\1\u0084",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "\1\u0087",
            "",
            "",
            "\12\u008b\1\u008a\2\u008b\1\u0089\37\u008b\1\u0088\uffd2\u008b",
            "\75\144\1\u008d\35\144\1\u008c\uffa4\144",
            "",
            "",
            "\1\u008e",
            "",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "\1\u0090",
            "\1\u0091",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "\1\u0093",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "",
            "",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "\12\u008b\1\u008a\2\u008b\1\u0089\37\u008b\1\u0097\uffd2\u008b",
            "\12\u0098\1\u008a\ufff5\u0098",
            "\0\u0098",
            "\12\u008b\1\u008a\2\u008b\1\u0089\37\u008b\1\u0088\uffd2\u008b",
            "\12\u009c\1\u009b\2\u009c\1\u009a\37\u009c\1\u0099\uffd2\u009c",
            "\75\144\1\u009e\35\144\1\u009d\uffa4\144",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "",
            "\1\u00a2",
            "",
            "",
            "",
            "\12\u008b\1\u008a\2\u008b\1\u0089\37\u008b\1\u0097\57\u008b"+
            "\1\u00a3\uffa2\u008b",
            "",
            "\12\u009c\1\u009b\2\u009c\1\u009a\37\u009c\1\u00a4\uffd2\u009c",
            "\12\u0098\1\u009b\ufff5\u0098",
            "\0\u0098",
            "\12\u009c\1\u009b\2\u009c\1\u009a\37\u009c\1\u0099\uffd2\u009c",
            "\12\u00a8\1\u00a6\2\u00a8\1\u00a5\37\u00a8\1\u00a7\uffd2\u00a8",
            "\133\144\1\u00a9\uffa4\144",
            "",
            "",
            "",
            "\1\u00aa",
            "\12\u008b\1\u008a\2\u008b\1\u0089\37\u008b\1\u0088\57\u008b"+
            "\1\u00ab\uffa2\u008b",
            "\12\u009c\1\u009b\2\u009c\1\u009a\37\u009c\1\u00a4\57\u009c"+
            "\1\u00ac\uffa2\u009c",
            "\12\u0098\1\u00a6\ufff5\u0098",
            "\0\u0098",
            "\12\u00a8\1\u00a6\2\u00a8\1\u00a5\37\u00a8\1\u00ad\uffd2\u00a8",
            "\12\u00a8\1\u00a6\2\u00a8\1\u00a5\37\u00a8\1\u00a7\uffd2\u00a8",
            "\12\u00b1\1\u00af\2\u00b1\1\u00ae\37\u00b1\1\u00b0\uffd2\u00b1",
            "\12\43\7\uffff\32\43\4\uffff\1\43\1\uffff\32\43",
            "\12\u008b\1\u008a\2\u008b\1\u0089\37\u008b\1\u0088\uffd2\u008b",
            "\12\u009c\1\u009b\2\u009c\1\u009a\37\u009c\1\u0099\17\u009c"+
            "\1\u00b3\uffc2\u009c",
            "\12\u00a8\1\u00a6\2\u00a8\1\u00a5\37\u00a8\1\u00ad\57\u00a8"+
            "\1\u00b4\uffa2\u00a8",
            "\12\u0098\1\u00af\ufff5\u0098",
            "\0\u0098",
            "\12\u00b1\1\u00af\2\u00b1\1\u00ae\37\u00b1\1\u00b5\uffd2\u00b1",
            "\12\u00b1\1\u00af\2\u00b1\1\u00ae\37\u00b1\1\u00b0\uffd2\u00b1",
            "",
            "\12\u009c\1\u009b\2\u009c\1\u009a\37\u009c\1\u0099\17\u009c"+
            "\1\u00b6\uffc2\u009c",
            "\12\u00a8\1\u00a6\2\u00a8\1\u00a5\37\u00a8\1\u00a7\17\u00a8"+
            "\1\u00b7\uffc2\u00a8",
            "\12\u00b1\1\u00af\2\u00b1\1\u00ae\37\u00b1\1\u00b5\57\u00b1"+
            "\1\u00b8\uffa2\u00b1",
            "\12\u009c\1\u009b\2\u009c\1\u009a\37\u009c\1\u0099\57\u009c"+
            "\1\u00b9\uffa2\u009c",
            "\12\u00a8\1\u00a6\2\u00a8\1\u00a5\37\u00a8\1\u00a7\17\u00a8"+
            "\1\u00ba\uffc2\u00a8",
            "\12\u00b1\1\u00af\2\u00b1\1\u00ae\37\u00b1\1\u00b0\17\u00b1"+
            "\1\u00bb\uffc2\u00b1",
            "\12\u009c\1\u009b\2\u009c\1\u009a\37\u009c\1\u0099\uffd2\u009c",
            "\12\u00a8\1\u00a6\2\u00a8\1\u00a5\37\u00a8\1\u00a7\57\u00a8"+
            "\1\u00bc\uffa2\u00a8",
            "\12\u00b1\1\u00af\2\u00b1\1\u00ae\37\u00b1\1\u00b0\17\u00b1"+
            "\1\u00bd\uffc2\u00b1",
            "\12\u00a8\1\u00a6\2\u00a8\1\u00a5\37\u00a8\1\u00a7\uffd2\u00a8",
            "\12\u00b1\1\u00af\2\u00b1\1\u00ae\37\u00b1\1\u00b0\17\u00b1"+
            "\1\u00be\uffc2\u00b1",
            "\12\u00b1\1\u00af\2\u00b1\1\u00ae\37\u00b1\1\u00b0\57\u00b1"+
            "\1\u00bf\uffa2\u00b1",
            "\12\u00b1\1\u00af\2\u00b1\1\u00ae\37\u00b1\1\u00b0\uffd2\u00b1"
    };

    static final short[] DFA22_eot = DFA.unpackEncodedString(DFA22_eotS);
    static final short[] DFA22_eof = DFA.unpackEncodedString(DFA22_eofS);
    static final char[] DFA22_min = DFA.unpackEncodedStringToUnsignedChars(DFA22_minS);
    static final char[] DFA22_max = DFA.unpackEncodedStringToUnsignedChars(DFA22_maxS);
    static final short[] DFA22_accept = DFA.unpackEncodedString(DFA22_acceptS);
    static final short[] DFA22_special = DFA.unpackEncodedString(DFA22_specialS);
    static final short[][] DFA22_transition;

    static {
        int numStates = DFA22_transitionS.length;
        DFA22_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA22_transition[i] = DFA.unpackEncodedString(DFA22_transitionS[i]);
        }
    }

    class DFA22 extends DFA {

        public DFA22(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 22;
            this.eot = DFA22_eot;
            this.eof = DFA22_eof;
            this.min = DFA22_min;
            this.max = DFA22_max;
            this.accept = DFA22_accept;
            this.special = DFA22_special;
            this.transition = DFA22_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | T__56 | T__57 | T__58 | T__59 | T__60 | T__61 | T__62 | T__63 | T__64 | T__65 | T__66 | NAME | INT | FLOAT1 | FLOAT2 | FLOAT3 | EXP | HEX | NORMALSTRING | CHARSTRING | LONGSTRING | COMMENT | LINE_COMMENT | WS | NEWLINE );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA22_151 = input.LA(1);

                        s = -1;
                        if ( (LA22_151==']') ) {s = 163;}

                        else if ( (LA22_151=='-') ) {s = 151;}

                        else if ( (LA22_151=='\r') ) {s = 137;}

                        else if ( (LA22_151=='\n') ) {s = 138;}

                        else if ( ((LA22_151>='\u0000' && LA22_151<='\t')||(LA22_151>='\u000B' && LA22_151<='\f')||(LA22_151>='\u000E' && LA22_151<=',')||(LA22_151>='.' && LA22_151<='\\')||(LA22_151>='^' && LA22_151<='\uFFFF')) ) {s = 139;}

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA22_158 = input.LA(1);

                        s = -1;
                        if ( (LA22_158=='[') ) {s = 169;}

                        else if ( ((LA22_158>='\u0000' && LA22_158<='Z')||(LA22_158>='\\' && LA22_158<='\uFFFF')) ) {s = 100;}

                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA22_141 = input.LA(1);

                        s = -1;
                        if ( (LA22_141=='[') ) {s = 157;}

                        else if ( (LA22_141=='=') ) {s = 158;}

                        else if ( ((LA22_141>='\u0000' && LA22_141<='<')||(LA22_141>='>' && LA22_141<='Z')||(LA22_141>='\\' && LA22_141<='\uFFFF')) ) {s = 100;}

                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA22_171 = input.LA(1);

                        s = -1;
                        if ( (LA22_171=='-') ) {s = 136;}

                        else if ( (LA22_171=='\r') ) {s = 137;}

                        else if ( (LA22_171=='\n') ) {s = 138;}

                        else if ( ((LA22_171>='\u0000' && LA22_171<='\t')||(LA22_171>='\u000B' && LA22_171<='\f')||(LA22_171>='\u000E' && LA22_171<=',')||(LA22_171>='.' && LA22_171<='\uFFFF')) ) {s = 139;}

                        else s = 152;

                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA22_165 = input.LA(1);

                        s = -1;
                        if ( ((LA22_165>='\u0000' && LA22_165<='\t')||(LA22_165>='\u000B' && LA22_165<='\uFFFF')) ) {s = 152;}

                        else if ( (LA22_165=='\n') ) {s = 166;}

                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA22_120 = input.LA(1);

                        s = -1;
                        if ( (LA22_120=='-') ) {s = 136;}

                        else if ( (LA22_120=='\r') ) {s = 137;}

                        else if ( (LA22_120=='\n') ) {s = 138;}

                        else if ( ((LA22_120>='\u0000' && LA22_120<='\t')||(LA22_120>='\u000B' && LA22_120<='\f')||(LA22_120>='\u000E' && LA22_120<=',')||(LA22_120>='.' && LA22_120<='\uFFFF')) ) {s = 139;}

                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA22_172 = input.LA(1);

                        s = -1;
                        if ( (LA22_172=='=') ) {s = 179;}

                        else if ( (LA22_172=='\r') ) {s = 154;}

                        else if ( (LA22_172=='\n') ) {s = 155;}

                        else if ( (LA22_172=='-') ) {s = 153;}

                        else if ( ((LA22_172>='\u0000' && LA22_172<='\t')||(LA22_172>='\u000B' && LA22_172<='\f')||(LA22_172>='\u000E' && LA22_172<=',')||(LA22_172>='.' && LA22_172<='<')||(LA22_172>='>' && LA22_172<='\uFFFF')) ) {s = 156;}

                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA22_182 = input.LA(1);

                        s = -1;
                        if ( (LA22_182==']') ) {s = 185;}

                        else if ( (LA22_182=='\r') ) {s = 154;}

                        else if ( (LA22_182=='\n') ) {s = 155;}

                        else if ( (LA22_182=='-') ) {s = 153;}

                        else if ( ((LA22_182>='\u0000' && LA22_182<='\t')||(LA22_182>='\u000B' && LA22_182<='\f')||(LA22_182>='\u000E' && LA22_182<=',')||(LA22_182>='.' && LA22_182<='\\')||(LA22_182>='^' && LA22_182<='\uFFFF')) ) {s = 156;}

                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA22_179 = input.LA(1);

                        s = -1;
                        if ( (LA22_179=='=') ) {s = 182;}

                        else if ( (LA22_179=='-') ) {s = 153;}

                        else if ( (LA22_179=='\r') ) {s = 154;}

                        else if ( (LA22_179=='\n') ) {s = 155;}

                        else if ( ((LA22_179>='\u0000' && LA22_179<='\t')||(LA22_179>='\u000B' && LA22_179<='\f')||(LA22_179>='\u000E' && LA22_179<=',')||(LA22_179>='.' && LA22_179<='<')||(LA22_179>='>' && LA22_179<='\uFFFF')) ) {s = 156;}

                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA22_139 = input.LA(1);

                        s = -1;
                        if ( (LA22_139=='-') ) {s = 136;}

                        else if ( (LA22_139=='\r') ) {s = 137;}

                        else if ( (LA22_139=='\n') ) {s = 138;}

                        else if ( ((LA22_139>='\u0000' && LA22_139<='\t')||(LA22_139>='\u000B' && LA22_139<='\f')||(LA22_139>='\u000E' && LA22_139<=',')||(LA22_139>='.' && LA22_139<='\uFFFF')) ) {s = 139;}

                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA22_153 = input.LA(1);

                        s = -1;
                        if ( (LA22_153=='-') ) {s = 164;}

                        else if ( (LA22_153=='\r') ) {s = 154;}

                        else if ( (LA22_153=='\n') ) {s = 155;}

                        else if ( ((LA22_153>='\u0000' && LA22_153<='\t')||(LA22_153>='\u000B' && LA22_153<='\f')||(LA22_153>='\u000E' && LA22_153<=',')||(LA22_153>='.' && LA22_153<='\uFFFF')) ) {s = 156;}

                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA22_175 = input.LA(1);

                        s = -1;
                        if ( ((LA22_175>='\u0000' && LA22_175<='\uFFFF')) ) {s = 152;}

                        else s = 100;

                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA22_163 = input.LA(1);

                        s = -1;
                        if ( (LA22_163==']') ) {s = 171;}

                        else if ( (LA22_163=='-') ) {s = 136;}

                        else if ( (LA22_163=='\r') ) {s = 137;}

                        else if ( (LA22_163=='\n') ) {s = 138;}

                        else if ( ((LA22_163>='\u0000' && LA22_163<='\t')||(LA22_163>='\u000B' && LA22_163<='\f')||(LA22_163>='\u000E' && LA22_163<=',')||(LA22_163>='.' && LA22_163<='\\')||(LA22_163>='^' && LA22_163<='\uFFFF')) ) {s = 139;}

                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA22_136 = input.LA(1);

                        s = -1;
                        if ( (LA22_136=='-') ) {s = 151;}

                        else if ( (LA22_136=='\r') ) {s = 137;}

                        else if ( (LA22_136=='\n') ) {s = 138;}

                        else if ( ((LA22_136>='\u0000' && LA22_136<='\t')||(LA22_136>='\u000B' && LA22_136<='\f')||(LA22_136>='\u000E' && LA22_136<=',')||(LA22_136>='.' && LA22_136<='\uFFFF')) ) {s = 139;}

                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA22_154 = input.LA(1);

                        s = -1;
                        if ( ((LA22_154>='\u0000' && LA22_154<='\t')||(LA22_154>='\u000B' && LA22_154<='\uFFFF')) ) {s = 152;}

                        else if ( (LA22_154=='\n') ) {s = 155;}

                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA22_191 = input.LA(1);

                        s = -1;
                        if ( (LA22_191=='\r') ) {s = 174;}

                        else if ( (LA22_191=='\n') ) {s = 175;}

                        else if ( (LA22_191=='-') ) {s = 176;}

                        else if ( ((LA22_191>='\u0000' && LA22_191<='\t')||(LA22_191>='\u000B' && LA22_191<='\f')||(LA22_191>='\u000E' && LA22_191<=',')||(LA22_191>='.' && LA22_191<='\uFFFF')) ) {s = 177;}

                        else s = 152;

                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA22_186 = input.LA(1);

                        s = -1;
                        if ( (LA22_186==']') ) {s = 188;}

                        else if ( (LA22_186=='\r') ) {s = 165;}

                        else if ( (LA22_186=='\n') ) {s = 166;}

                        else if ( (LA22_186=='-') ) {s = 167;}

                        else if ( ((LA22_186>='\u0000' && LA22_186<='\t')||(LA22_186>='\u000B' && LA22_186<='\f')||(LA22_186>='\u000E' && LA22_186<=',')||(LA22_186>='.' && LA22_186<='\\')||(LA22_186>='^' && LA22_186<='\uFFFF')) ) {s = 168;}

                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA22_183 = input.LA(1);

                        s = -1;
                        if ( (LA22_183=='=') ) {s = 186;}

                        else if ( (LA22_183=='\r') ) {s = 165;}

                        else if ( (LA22_183=='\n') ) {s = 166;}

                        else if ( (LA22_183=='-') ) {s = 167;}

                        else if ( ((LA22_183>='\u0000' && LA22_183<='\t')||(LA22_183>='\u000B' && LA22_183<='\f')||(LA22_183>='\u000E' && LA22_183<=',')||(LA22_183>='.' && LA22_183<='<')||(LA22_183>='>' && LA22_183<='\uFFFF')) ) {s = 168;}

                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA22_140 = input.LA(1);

                        s = -1;
                        if ( (LA22_140=='-') ) {s = 153;}

                        else if ( (LA22_140=='\r') ) {s = 154;}

                        else if ( (LA22_140=='\n') ) {s = 155;}

                        else if ( ((LA22_140>='\u0000' && LA22_140<='\t')||(LA22_140>='\u000B' && LA22_140<='\f')||(LA22_140>='\u000E' && LA22_140<=',')||(LA22_140>='.' && LA22_140<='\uFFFF')) ) {s = 156;}

                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA22_167 = input.LA(1);

                        s = -1;
                        if ( (LA22_167=='-') ) {s = 173;}

                        else if ( (LA22_167=='\r') ) {s = 165;}

                        else if ( (LA22_167=='\n') ) {s = 166;}

                        else if ( ((LA22_167>='\u0000' && LA22_167<='\t')||(LA22_167>='\u000B' && LA22_167<='\f')||(LA22_167>='\u000E' && LA22_167<=',')||(LA22_167>='.' && LA22_167<='\uFFFF')) ) {s = 168;}

                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA22_180 = input.LA(1);

                        s = -1;
                        if ( (LA22_180=='=') ) {s = 183;}

                        else if ( (LA22_180=='\r') ) {s = 165;}

                        else if ( (LA22_180=='\n') ) {s = 166;}

                        else if ( (LA22_180=='-') ) {s = 167;}

                        else if ( ((LA22_180>='\u0000' && LA22_180<='\t')||(LA22_180>='\u000B' && LA22_180<='\f')||(LA22_180>='\u000E' && LA22_180<=',')||(LA22_180>='.' && LA22_180<='<')||(LA22_180>='>' && LA22_180<='\uFFFF')) ) {s = 168;}

                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA22_164 = input.LA(1);

                        s = -1;
                        if ( (LA22_164==']') ) {s = 172;}

                        else if ( (LA22_164=='-') ) {s = 164;}

                        else if ( (LA22_164=='\r') ) {s = 154;}

                        else if ( (LA22_164=='\n') ) {s = 155;}

                        else if ( ((LA22_164>='\u0000' && LA22_164<='\t')||(LA22_164>='\u000B' && LA22_164<='\f')||(LA22_164>='\u000E' && LA22_164<=',')||(LA22_164>='.' && LA22_164<='\\')||(LA22_164>='^' && LA22_164<='\uFFFF')) ) {s = 156;}

                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA22_137 = input.LA(1);

                        s = -1;
                        if ( ((LA22_137>='\u0000' && LA22_137<='\t')||(LA22_137>='\u000B' && LA22_137<='\uFFFF')) ) {s = 152;}

                        else if ( (LA22_137=='\n') ) {s = 138;}

                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA22_177 = input.LA(1);

                        s = -1;
                        if ( (LA22_177=='\r') ) {s = 174;}

                        else if ( (LA22_177=='\n') ) {s = 175;}

                        else if ( (LA22_177=='-') ) {s = 176;}

                        else if ( ((LA22_177>='\u0000' && LA22_177<='\t')||(LA22_177>='\u000B' && LA22_177<='\f')||(LA22_177>='\u000E' && LA22_177<=',')||(LA22_177>='.' && LA22_177<='\uFFFF')) ) {s = 177;}

                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA22_176 = input.LA(1);

                        s = -1;
                        if ( (LA22_176=='-') ) {s = 181;}

                        else if ( (LA22_176=='\r') ) {s = 174;}

                        else if ( (LA22_176=='\n') ) {s = 175;}

                        else if ( ((LA22_176>='\u0000' && LA22_176<='\t')||(LA22_176>='\u000B' && LA22_176<='\f')||(LA22_176>='\u000E' && LA22_176<=',')||(LA22_176>='.' && LA22_176<='\uFFFF')) ) {s = 177;}

                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA22_187 = input.LA(1);

                        s = -1;
                        if ( (LA22_187=='=') ) {s = 189;}

                        else if ( (LA22_187=='\r') ) {s = 174;}

                        else if ( (LA22_187=='\n') ) {s = 175;}

                        else if ( (LA22_187=='-') ) {s = 176;}

                        else if ( ((LA22_187>='\u0000' && LA22_187<='\t')||(LA22_187>='\u000B' && LA22_187<='\f')||(LA22_187>='\u000E' && LA22_187<=',')||(LA22_187>='.' && LA22_187<='<')||(LA22_187>='>' && LA22_187<='\uFFFF')) ) {s = 177;}

                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA22_184 = input.LA(1);

                        s = -1;
                        if ( (LA22_184=='=') ) {s = 187;}

                        else if ( (LA22_184=='\r') ) {s = 174;}

                        else if ( (LA22_184=='\n') ) {s = 175;}

                        else if ( (LA22_184=='-') ) {s = 176;}

                        else if ( ((LA22_184>='\u0000' && LA22_184<='\t')||(LA22_184>='\u000B' && LA22_184<='\f')||(LA22_184>='\u000E' && LA22_184<=',')||(LA22_184>='.' && LA22_184<='<')||(LA22_184>='>' && LA22_184<='\uFFFF')) ) {s = 177;}

                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA22_155 = input.LA(1);

                        s = -1;
                        if ( ((LA22_155>='\u0000' && LA22_155<='\uFFFF')) ) {s = 152;}

                        else s = 100;

                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA22_66 = input.LA(1);

                        s = -1;
                        if ( (LA22_66=='[') ) {s = 99;}

                        else if ( ((LA22_66>='\u0000' && LA22_66<='Z')||(LA22_66>='\\' && LA22_66<='\uFFFF')) ) {s = 100;}

                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA22_173 = input.LA(1);

                        s = -1;
                        if ( (LA22_173==']') ) {s = 180;}

                        else if ( (LA22_173=='-') ) {s = 173;}

                        else if ( (LA22_173=='\r') ) {s = 165;}

                        else if ( (LA22_173=='\n') ) {s = 166;}

                        else if ( ((LA22_173>='\u0000' && LA22_173<='\t')||(LA22_173>='\u000B' && LA22_173<='\f')||(LA22_173>='\u000E' && LA22_173<=',')||(LA22_173>='.' && LA22_173<='\\')||(LA22_173>='^' && LA22_173<='\uFFFF')) ) {s = 168;}

                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA22_188 = input.LA(1);

                        s = -1;
                        if ( (LA22_188=='\r') ) {s = 165;}

                        else if ( (LA22_188=='\n') ) {s = 166;}

                        else if ( (LA22_188=='-') ) {s = 167;}

                        else if ( ((LA22_188>='\u0000' && LA22_188<='\t')||(LA22_188>='\u000B' && LA22_188<='\f')||(LA22_188>='\u000E' && LA22_188<=',')||(LA22_188>='.' && LA22_188<='\uFFFF')) ) {s = 168;}

                        else s = 152;

                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA22_189 = input.LA(1);

                        s = -1;
                        if ( (LA22_189=='=') ) {s = 190;}

                        else if ( (LA22_189=='\r') ) {s = 174;}

                        else if ( (LA22_189=='\n') ) {s = 175;}

                        else if ( (LA22_189=='-') ) {s = 176;}

                        else if ( ((LA22_189>='\u0000' && LA22_189<='\t')||(LA22_189>='\u000B' && LA22_189<='\f')||(LA22_189>='\u000E' && LA22_189<=',')||(LA22_189>='.' && LA22_189<='<')||(LA22_189>='>' && LA22_189<='\uFFFF')) ) {s = 177;}

                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA22_190 = input.LA(1);

                        s = -1;
                        if ( (LA22_190==']') ) {s = 191;}

                        else if ( (LA22_190=='\r') ) {s = 174;}

                        else if ( (LA22_190=='\n') ) {s = 175;}

                        else if ( (LA22_190=='-') ) {s = 176;}

                        else if ( ((LA22_190>='\u0000' && LA22_190<='\t')||(LA22_190>='\u000B' && LA22_190<='\f')||(LA22_190>='\u000E' && LA22_190<=',')||(LA22_190>='.' && LA22_190<='\\')||(LA22_190>='^' && LA22_190<='\uFFFF')) ) {s = 177;}

                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA22_157 = input.LA(1);

                        s = -1;
                        if ( (LA22_157=='\r') ) {s = 165;}

                        else if ( (LA22_157=='\n') ) {s = 166;}

                        else if ( (LA22_157=='-') ) {s = 167;}

                        else if ( ((LA22_157>='\u0000' && LA22_157<='\t')||(LA22_157>='\u000B' && LA22_157<='\f')||(LA22_157>='\u000E' && LA22_157<=',')||(LA22_157>='.' && LA22_157<='\uFFFF')) ) {s = 168;}

                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA22_138 = input.LA(1);

                        s = -1;
                        if ( ((LA22_138>='\u0000' && LA22_138<='\uFFFF')) ) {s = 152;}

                        else s = 100;

                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA22_181 = input.LA(1);

                        s = -1;
                        if ( (LA22_181==']') ) {s = 184;}

                        else if ( (LA22_181=='-') ) {s = 181;}

                        else if ( (LA22_181=='\r') ) {s = 174;}

                        else if ( (LA22_181=='\n') ) {s = 175;}

                        else if ( ((LA22_181>='\u0000' && LA22_181<='\t')||(LA22_181>='\u000B' && LA22_181<='\f')||(LA22_181>='\u000E' && LA22_181<=',')||(LA22_181>='.' && LA22_181<='\\')||(LA22_181>='^' && LA22_181<='\uFFFF')) ) {s = 177;}

                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA22_185 = input.LA(1);

                        s = -1;
                        if ( (LA22_185=='-') ) {s = 153;}

                        else if ( (LA22_185=='\r') ) {s = 154;}

                        else if ( (LA22_185=='\n') ) {s = 155;}

                        else if ( ((LA22_185>='\u0000' && LA22_185<='\t')||(LA22_185>='\u000B' && LA22_185<='\f')||(LA22_185>='\u000E' && LA22_185<=',')||(LA22_185>='.' && LA22_185<='\uFFFF')) ) {s = 156;}

                        else s = 152;

                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA22_168 = input.LA(1);

                        s = -1;
                        if ( (LA22_168=='\r') ) {s = 165;}

                        else if ( (LA22_168=='\n') ) {s = 166;}

                        else if ( (LA22_168=='-') ) {s = 167;}

                        else if ( ((LA22_168>='\u0000' && LA22_168<='\t')||(LA22_168>='\u000B' && LA22_168<='\f')||(LA22_168>='\u000E' && LA22_168<=',')||(LA22_168>='.' && LA22_168<='\uFFFF')) ) {s = 168;}

                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA22_169 = input.LA(1);

                        s = -1;
                        if ( (LA22_169=='\r') ) {s = 174;}

                        else if ( (LA22_169=='\n') ) {s = 175;}

                        else if ( (LA22_169=='-') ) {s = 176;}

                        else if ( ((LA22_169>='\u0000' && LA22_169<='\t')||(LA22_169>='\u000B' && LA22_169<='\f')||(LA22_169>='\u000E' && LA22_169<=',')||(LA22_169>='.' && LA22_169<='\uFFFF')) ) {s = 177;}

                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA22_174 = input.LA(1);

                        s = -1;
                        if ( ((LA22_174>='\u0000' && LA22_174<='\t')||(LA22_174>='\u000B' && LA22_174<='\uFFFF')) ) {s = 152;}

                        else if ( (LA22_174=='\n') ) {s = 175;}

                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA22_99 = input.LA(1);

                        s = -1;
                        if ( (LA22_99=='[') ) {s = 120;}

                        else if ( (LA22_99=='=') ) {s = 121;}

                        else if ( ((LA22_99>='\u0000' && LA22_99<='<')||(LA22_99>='>' && LA22_99<='Z')||(LA22_99>='\\' && LA22_99<='\uFFFF')) ) {s = 100;}

                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA22_166 = input.LA(1);

                        s = -1;
                        if ( ((LA22_166>='\u0000' && LA22_166<='\uFFFF')) ) {s = 152;}

                        else s = 100;

                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA22_121 = input.LA(1);

                        s = -1;
                        if ( (LA22_121=='[') ) {s = 140;}

                        else if ( (LA22_121=='=') ) {s = 141;}

                        else if ( ((LA22_121>='\u0000' && LA22_121<='<')||(LA22_121>='>' && LA22_121<='Z')||(LA22_121>='\\' && LA22_121<='\uFFFF')) ) {s = 100;}

                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA22_156 = input.LA(1);

                        s = -1;
                        if ( (LA22_156=='\r') ) {s = 154;}

                        else if ( (LA22_156=='\n') ) {s = 155;}

                        else if ( (LA22_156=='-') ) {s = 153;}

                        else if ( ((LA22_156>='\u0000' && LA22_156<='\t')||(LA22_156>='\u000B' && LA22_156<='\f')||(LA22_156>='\u000E' && LA22_156<=',')||(LA22_156>='.' && LA22_156<='\uFFFF')) ) {s = 156;}

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 22, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

}