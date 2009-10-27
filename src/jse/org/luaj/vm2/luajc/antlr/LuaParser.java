// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 Lua.g 2009-10-19 10:13:33
 
	package org.luaj.vm2.luajc.antlr;
	import org.luaj.vm2.luajc.lst.*; 


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
public class LuaParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "NAME", "INT", "FLOAT1", "FLOAT2", "FLOAT3", "EXP", "HEX", "NORMALSTRING", "CHARSTRING", "LONGSTRING", "EscapeSequence", "DecimalEscape", "COMMENT", "LINE_COMMENT", "WS", "NEWLINE", "';'", "'='", "'do'", "'end'", "'while'", "'repeat'", "'until'", "'for'", "','", "'in'", "'function'", "'local'", "'if'", "'then'", "'elseif'", "'else'", "'return'", "'break'", "'.'", "':'", "'nil'", "'false'", "'true'", "'...'", "'('", "')'", "'['", "']'", "'{'", "'}'", "'+'", "'-'", "'*'", "'/'", "'^'", "'%'", "'..'", "'<'", "'<='", "'>'", "'>='", "'=='", "'~='", "'and'", "'or'", "'not'", "'#'"
    };
    public static final int T__66=66;
    public static final int T__64=64;
    public static final int T__29=29;
    public static final int T__65=65;
    public static final int T__28=28;
    public static final int T__62=62;
    public static final int T__27=27;
    public static final int T__63=63;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int T__61=61;
    public static final int T__60=60;
    public static final int EOF=-1;
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
    public static final int CHARSTRING=12;
    public static final int INT=5;
    public static final int LONGSTRING=13;
    public static final int T__30=30;
    public static final int NORMALSTRING=11;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int WS=18;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int NEWLINE=19;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int EscapeSequence=14;

    // delegates
    // delegators


        public LuaParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public LuaParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return LuaParser.tokenNames; }
    public String getGrammarFileName() { return "Lua.g"; }


    	LSChunk CHK = null;



    // $ANTLR start "chunk"
    // Lua.g:22:1: chunk[String chunkname] returns [LSChunk c] : funcblock[CHK.function] ;
    public final LSChunk chunk(String chunkname) throws RecognitionException {
        LSChunk c = null;

         CHK = new LSChunk(chunkname); 
        try {
            // Lua.g:24:2: ( funcblock[CHK.function] )
            // Lua.g:24:4: funcblock[CHK.function]
            {
            pushFollow(FOLLOW_funcblock_in_chunk58);
            funcblock(CHK.function);

            state._fsp--;
            if (state.failed) return c;
            if ( state.backtracking==0 ) {
              c =CHK;
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return c;
    }
    // $ANTLR end "chunk"

    protected static class funcblock_scope {
        LSFunction func;
    }
    protected Stack funcblock_stack = new Stack();


    // $ANTLR start "funcblock"
    // Lua.g:27:1: funcblock[LSFunction f] : block ;
    public final void funcblock(LSFunction f) throws RecognitionException {
        funcblock_stack.push(new funcblock_scope());
        List<LSStatement> block1 = null;


         ((funcblock_scope)funcblock_stack.peek()).func = f;  
        try {
            // Lua.g:30:2: ( block )
            // Lua.g:30:4: block
            {
            if ( state.backtracking==0 ) {
              CHK.pushScope("body");
            }
            pushFollow(FOLLOW_block_in_funcblock88);
            block1=block();

            state._fsp--;
            if (state.failed) return ;
            if ( state.backtracking==0 ) {
              f.setStatements(block1); CHK.popScope("body"); 
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            funcblock_stack.pop();
        }
        return ;
    }
    // $ANTLR end "funcblock"


    // $ANTLR start "block"
    // Lua.g:33:1: block returns [List<LSStatement> stats] : ( stat ( ';' )? )* ( laststat ( ';' )? )? ;
    public final List<LSStatement> block() throws RecognitionException {
        List<LSStatement> stats = null;

        LSStatement stat2 = null;

        LSStatement laststat3 = null;


         stats = new ArrayList<LSStatement>(); 
        try {
            // Lua.g:35:2: ( ( stat ( ';' )? )* ( laststat ( ';' )? )? )
            // Lua.g:35:4: ( stat ( ';' )? )* ( laststat ( ';' )? )?
            {
            // Lua.g:35:4: ( stat ( ';' )? )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==NAME||LA2_0==22||(LA2_0>=24 && LA2_0<=25)||LA2_0==27||(LA2_0>=30 && LA2_0<=32)||LA2_0==44) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // Lua.g:35:5: stat ( ';' )?
            	    {
            	    pushFollow(FOLLOW_stat_in_block113);
            	    stat2=stat();

            	    state._fsp--;
            	    if (state.failed) return stats;
            	    if ( state.backtracking==0 ) {
            	      stats.add(stat2);
            	    }
            	    // Lua.g:35:33: ( ';' )?
            	    int alt1=2;
            	    int LA1_0 = input.LA(1);

            	    if ( (LA1_0==20) ) {
            	        alt1=1;
            	    }
            	    switch (alt1) {
            	        case 1 :
            	            // Lua.g:35:34: ';'
            	            {
            	            match(input,20,FOLLOW_20_in_block118); if (state.failed) return stats;

            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            // Lua.g:36:3: ( laststat ( ';' )? )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( ((LA4_0>=36 && LA4_0<=37)) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // Lua.g:36:4: laststat ( ';' )?
                    {
                    pushFollow(FOLLOW_laststat_in_block128);
                    laststat3=laststat();

                    state._fsp--;
                    if (state.failed) return stats;
                    if ( state.backtracking==0 ) {
                      stats.add(laststat3);
                    }
                    // Lua.g:36:40: ( ';' )?
                    int alt3=2;
                    int LA3_0 = input.LA(1);

                    if ( (LA3_0==20) ) {
                        alt3=1;
                    }
                    switch (alt3) {
                        case 1 :
                            // Lua.g:36:41: ';'
                            {
                            match(input,20,FOLLOW_20_in_block133); if (state.failed) return stats;

                            }
                            break;

                    }


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return stats;
    }
    // $ANTLR end "block"


    // $ANTLR start "stat"
    // Lua.g:39:1: stat returns [LSStatement s] : ( varlist1 '=' explist1 | functioncall | 'do' block 'end' | 'while' exp 'do' block 'end' | 'repeat' block 'until' exp | ifstat | 'for' NAME '=' e1= exp ',' e2= exp ( ',' e3= exp )? 'do' block 'end' | 'for' namelist 'in' explist1 'do' block 'end' | 'function' funcname funcbody | 'local' 'function' NAME funcbody | 'local' namelist ( '=' explist1 )? );
    public final LSStatement stat() throws RecognitionException {
        LSStatement s = null;

        Token NAME13=null;
        Token NAME20=null;
        LSExpression e1 = null;

        LSExpression e2 = null;

        LSExpression e3 = null;

        List<LSVariable> varlist14 = null;

        List<LSExpression> explist15 = null;

        LSVariable functioncall6 = null;

        List<LSStatement> block7 = null;

        LSExpression exp8 = null;

        List<LSStatement> block9 = null;

        List<LSStatement> block10 = null;

        LSExpression exp11 = null;

        LSStatement ifstat12 = null;

        List<LSStatement> block14 = null;

        List<String> namelist15 = null;

        List<LSExpression> explist116 = null;

        List<LSStatement> block17 = null;

        LSVariable funcname18 = null;

        LSFunction funcbody19 = null;

        LSFunction funcbody21 = null;

        List<String> namelist22 = null;

        List<LSExpression> explist123 = null;


         Name name=null; List<Name> names=null; 
        try {
            // Lua.g:41:2: ( varlist1 '=' explist1 | functioncall | 'do' block 'end' | 'while' exp 'do' block 'end' | 'repeat' block 'until' exp | ifstat | 'for' NAME '=' e1= exp ',' e2= exp ( ',' e3= exp )? 'do' block 'end' | 'for' namelist 'in' explist1 'do' block 'end' | 'function' funcname funcbody | 'local' 'function' NAME funcbody | 'local' namelist ( '=' explist1 )? )
            int alt7=11;
            alt7 = dfa7.predict(input);
            switch (alt7) {
                case 1 :
                    // Lua.g:41:4: varlist1 '=' explist1
                    {
                    pushFollow(FOLLOW_varlist1_in_stat159);
                    varlist14=varlist1();

                    state._fsp--;
                    if (state.failed) return s;
                    match(input,21,FOLLOW_21_in_stat161); if (state.failed) return s;
                    pushFollow(FOLLOW_explist1_in_stat163);
                    explist15=explist1();

                    state._fsp--;
                    if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                       s =LSStatement.varAssignStatement(varlist14,explist15, CHK.peekScope(), ((funcblock_scope)funcblock_stack.peek()).func); 
                    }

                    }
                    break;
                case 2 :
                    // Lua.g:42:4: functioncall
                    {
                    pushFollow(FOLLOW_functioncall_in_stat183);
                    functioncall6=functioncall();

                    state._fsp--;
                    if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                       s =LSStatement.functionCallStatement(functioncall6); 
                    }

                    }
                    break;
                case 3 :
                    // Lua.g:43:4: 'do' block 'end'
                    {
                    match(input,22,FOLLOW_22_in_stat213); if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      CHK.pushScope("do");
                    }
                    pushFollow(FOLLOW_block_in_stat217);
                    block7=block();

                    state._fsp--;
                    if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      CHK.popScope("do");
                    }
                    match(input,23,FOLLOW_23_in_stat221); if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                       s =LSStatement.doBlockStatement(block7); 
                    }

                    }
                    break;
                case 4 :
                    // Lua.g:45:4: 'while' exp 'do' block 'end'
                    {
                    match(input,24,FOLLOW_24_in_stat286); if (state.failed) return s;
                    pushFollow(FOLLOW_exp_in_stat288);
                    exp8=exp();

                    state._fsp--;
                    if (state.failed) return s;
                    match(input,22,FOLLOW_22_in_stat290); if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      CHK.pushScope("while");
                    }
                    pushFollow(FOLLOW_block_in_stat294);
                    block9=block();

                    state._fsp--;
                    if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      CHK.popScope("while");
                    }
                    match(input,23,FOLLOW_23_in_stat298); if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                       s =LSStatement.whileLoopStatement(exp8,block9); 
                    }

                    }
                    break;
                case 5 :
                    // Lua.g:47:4: 'repeat' block 'until' exp
                    {
                    match(input,25,FOLLOW_25_in_stat344); if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      CHK.pushScope("repeat");
                    }
                    pushFollow(FOLLOW_block_in_stat349);
                    block10=block();

                    state._fsp--;
                    if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      CHK.popScope("repeat");
                    }
                    match(input,26,FOLLOW_26_in_stat354); if (state.failed) return s;
                    pushFollow(FOLLOW_exp_in_stat356);
                    exp11=exp();

                    state._fsp--;
                    if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                       s =LSStatement.repeatUntilStatement(block10,exp11); 
                    }

                    }
                    break;
                case 6 :
                    // Lua.g:49:4: ifstat
                    {
                    pushFollow(FOLLOW_ifstat_in_stat410);
                    ifstat12=ifstat();

                    state._fsp--;
                    if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                       s =ifstat12; 
                    }

                    }
                    break;
                case 7 :
                    // Lua.g:50:4: 'for' NAME '=' e1= exp ',' e2= exp ( ',' e3= exp )? 'do' block 'end'
                    {
                    match(input,27,FOLLOW_27_in_stat445); if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      CHK.pushScope("fori");
                    }
                    NAME13=(Token)match(input,NAME,FOLLOW_NAME_in_stat449); if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      name=CHK.declare((NAME13!=null?NAME13.getText():null));
                    }
                    match(input,21,FOLLOW_21_in_stat453); if (state.failed) return s;
                    pushFollow(FOLLOW_exp_in_stat457);
                    e1=exp();

                    state._fsp--;
                    if (state.failed) return s;
                    match(input,28,FOLLOW_28_in_stat459); if (state.failed) return s;
                    pushFollow(FOLLOW_exp_in_stat463);
                    e2=exp();

                    state._fsp--;
                    if (state.failed) return s;
                    // Lua.g:50:94: ( ',' e3= exp )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);

                    if ( (LA5_0==28) ) {
                        alt5=1;
                    }
                    switch (alt5) {
                        case 1 :
                            // Lua.g:50:95: ',' e3= exp
                            {
                            match(input,28,FOLLOW_28_in_stat466); if (state.failed) return s;
                            pushFollow(FOLLOW_exp_in_stat470);
                            e3=exp();

                            state._fsp--;
                            if (state.failed) return s;

                            }
                            break;

                    }

                    match(input,22,FOLLOW_22_in_stat474); if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      CHK.pushScope("foriblock");
                    }
                    pushFollow(FOLLOW_block_in_stat478);
                    block14=block();

                    state._fsp--;
                    if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      CHK.popScope("foriblock");
                    }
                    match(input,23,FOLLOW_23_in_stat482); if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                       s =LSStatement.forLoopStatement(name,e1,e2,e3,block14,CHK.peekScope()); CHK.popScope("fori"); 
                    }

                    }
                    break;
                case 8 :
                    // Lua.g:52:4: 'for' namelist 'in' explist1 'do' block 'end'
                    {
                    match(input,27,FOLLOW_27_in_stat528); if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      CHK.pushScope("for");
                    }
                    pushFollow(FOLLOW_namelist_in_stat533);
                    namelist15=namelist();

                    state._fsp--;
                    if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      names=CHK.declare(namelist15);
                    }
                    match(input,29,FOLLOW_29_in_stat537); if (state.failed) return s;
                    pushFollow(FOLLOW_explist1_in_stat539);
                    explist116=explist1();

                    state._fsp--;
                    if (state.failed) return s;
                    match(input,22,FOLLOW_22_in_stat541); if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      CHK.pushScope("forblock");
                    }
                    pushFollow(FOLLOW_block_in_stat545);
                    block17=block();

                    state._fsp--;
                    if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      CHK.popScope("forblock");
                    }
                    match(input,23,FOLLOW_23_in_stat548); if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                       s =LSStatement.forListStatement(names,explist116,block17,CHK.peekScope(), ((funcblock_scope)funcblock_stack.peek()).func); CHK.popScope("for");
                    }

                    }
                    break;
                case 9 :
                    // Lua.g:54:4: 'function' funcname funcbody
                    {
                    match(input,30,FOLLOW_30_in_stat593); if (state.failed) return s;
                    pushFollow(FOLLOW_funcname_in_stat595);
                    funcname18=funcname();

                    state._fsp--;
                    if (state.failed) return s;
                    pushFollow(FOLLOW_funcbody_in_stat597);
                    funcbody19=funcbody();

                    state._fsp--;
                    if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                       s =LSStatement.varFunctionStatement(funcname18,funcbody19); 
                    }

                    }
                    break;
                case 10 :
                    // Lua.g:55:4: 'local' 'function' NAME funcbody
                    {
                    match(input,31,FOLLOW_31_in_stat610); if (state.failed) return s;
                    match(input,30,FOLLOW_30_in_stat612); if (state.failed) return s;
                    NAME20=(Token)match(input,NAME,FOLLOW_NAME_in_stat614); if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      name=CHK.declare((NAME20!=null?NAME20.getText():null));
                    }
                    pushFollow(FOLLOW_funcbody_in_stat618);
                    funcbody21=funcbody();

                    state._fsp--;
                    if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                       s =LSStatement.localFunctionStatement(name,funcbody21); 
                    }

                    }
                    break;
                case 11 :
                    // Lua.g:57:4: 'local' namelist ( '=' explist1 )?
                    {
                    match(input,31,FOLLOW_31_in_stat666); if (state.failed) return s;
                    pushFollow(FOLLOW_namelist_in_stat668);
                    namelist22=namelist();

                    state._fsp--;
                    if (state.failed) return s;
                    // Lua.g:57:21: ( '=' explist1 )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==21) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // Lua.g:57:22: '=' explist1
                            {
                            match(input,21,FOLLOW_21_in_stat671); if (state.failed) return s;
                            pushFollow(FOLLOW_explist1_in_stat673);
                            explist123=explist1();

                            state._fsp--;
                            if (state.failed) return s;

                            }
                            break;

                    }

                    if ( state.backtracking==0 ) {
                       s =LSStatement.localAssignStatement(CHK.declare(namelist22),explist123,CHK.peekScope(), ((funcblock_scope)funcblock_stack.peek()).func); 
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return s;
    }
    // $ANTLR end "stat"

    protected static class ifstat_scope {
        LSIfStatement current;
    }
    protected Stack ifstat_stack = new Stack();


    // $ANTLR start "ifstat"
    // Lua.g:60:1: ifstat returns [LSStatement s] : 'if' e1= exp 'then' b1= block ( 'elseif' e2= exp 'then' b2= block )* ( 'else' b3= block )? 'end' ;
    public final LSStatement ifstat() throws RecognitionException {
        ifstat_stack.push(new ifstat_scope());
        LSStatement s = null;

        LSExpression e1 = null;

        List<LSStatement> b1 = null;

        LSExpression e2 = null;

        List<LSStatement> b2 = null;

        List<LSStatement> b3 = null;


        try {
            // Lua.g:62:2: ( 'if' e1= exp 'then' b1= block ( 'elseif' e2= exp 'then' b2= block )* ( 'else' b3= block )? 'end' )
            // Lua.g:62:4: 'if' e1= exp 'then' b1= block ( 'elseif' e2= exp 'then' b2= block )* ( 'else' b3= block )? 'end'
            {
            match(input,32,FOLLOW_32_in_ifstat699); if (state.failed) return s;
            pushFollow(FOLLOW_exp_in_ifstat703);
            e1=exp();

            state._fsp--;
            if (state.failed) return s;
            match(input,33,FOLLOW_33_in_ifstat705); if (state.failed) return s;
            if ( state.backtracking==0 ) {
              CHK.pushScope("if");
            }
            pushFollow(FOLLOW_block_in_ifstat712);
            b1=block();

            state._fsp--;
            if (state.failed) return s;
            if ( state.backtracking==0 ) {
              ((ifstat_scope)ifstat_stack.peek()).current =new LSIfStatement(e1,b1); CHK.popScope("if");
            }
            // Lua.g:63:4: ( 'elseif' e2= exp 'then' b2= block )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==34) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // Lua.g:63:5: 'elseif' e2= exp 'then' b2= block
            	    {
            	    match(input,34,FOLLOW_34_in_ifstat721); if (state.failed) return s;
            	    pushFollow(FOLLOW_exp_in_ifstat725);
            	    e2=exp();

            	    state._fsp--;
            	    if (state.failed) return s;
            	    match(input,33,FOLLOW_33_in_ifstat727); if (state.failed) return s;
            	    if ( state.backtracking==0 ) {
            	      CHK.pushScope("elseif");
            	    }
            	    pushFollow(FOLLOW_block_in_ifstat734);
            	    b2=block();

            	    state._fsp--;
            	    if (state.failed) return s;
            	    if ( state.backtracking==0 ) {
            	      ((ifstat_scope)ifstat_stack.peek()).current.addElseif(e2,b2); CHK.popScope("elseif");
            	    }

            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

            // Lua.g:64:4: ( 'else' b3= block )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==35) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // Lua.g:64:5: 'else' b3= block
                    {
                    match(input,35,FOLLOW_35_in_ifstat745); if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      CHK.pushScope("else");
                    }
                    pushFollow(FOLLOW_block_in_ifstat752);
                    b3=block();

                    state._fsp--;
                    if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      ((ifstat_scope)ifstat_stack.peek()).current.addElse(b3); CHK.popScope("else");
                    }

                    }
                    break;

            }

            match(input,23,FOLLOW_23_in_ifstat762); if (state.failed) return s;
            if ( state.backtracking==0 ) {
               s =((ifstat_scope)ifstat_stack.peek()).current; 
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            ifstat_stack.pop();
        }
        return s;
    }
    // $ANTLR end "ifstat"


    // $ANTLR start "laststat"
    // Lua.g:69:1: laststat returns [LSStatement s] : ( 'return' (e= explist1 )? | 'break' );
    public final LSStatement laststat() throws RecognitionException {
        LSStatement s = null;

        List<LSExpression> e = null;


        try {
            // Lua.g:70:2: ( 'return' (e= explist1 )? | 'break' )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==36) ) {
                alt11=1;
            }
            else if ( (LA11_0==37) ) {
                alt11=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return s;}
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // Lua.g:70:4: 'return' (e= explist1 )?
                    {
                    match(input,36,FOLLOW_36_in_laststat782); if (state.failed) return s;
                    // Lua.g:70:13: (e= explist1 )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( ((LA10_0>=NAME && LA10_0<=LONGSTRING)||LA10_0==30||(LA10_0>=40 && LA10_0<=44)||LA10_0==48||LA10_0==51||(LA10_0>=65 && LA10_0<=66)) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // Lua.g:70:14: e= explist1
                            {
                            pushFollow(FOLLOW_explist1_in_laststat787);
                            e=explist1();

                            state._fsp--;
                            if (state.failed) return s;

                            }
                            break;

                    }

                    if ( state.backtracking==0 ) {
                      s =LSStatement.returnStatement(((funcblock_scope)funcblock_stack.peek()).func,e);
                    }

                    }
                    break;
                case 2 :
                    // Lua.g:71:4: 'break'
                    {
                    match(input,37,FOLLOW_37_in_laststat797); if (state.failed) return s;
                    if ( state.backtracking==0 ) {
                      s =LSStatement.breakStatement();
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return s;
    }
    // $ANTLR end "laststat"


    // $ANTLR start "funcname"
    // Lua.g:74:1: funcname returns [LSVariable v] : n= NAME ( '.' n2= NAME )* ( ':' n3= NAME )? ;
    public final LSVariable funcname() throws RecognitionException {
        LSVariable v = null;

        Token n=null;
        Token n2=null;
        Token n3=null;

        try {
            // Lua.g:75:2: (n= NAME ( '.' n2= NAME )* ( ':' n3= NAME )? )
            // Lua.g:75:4: n= NAME ( '.' n2= NAME )* ( ':' n3= NAME )?
            {
            n=(Token)match(input,NAME,FOLLOW_NAME_in_funcname831); if (state.failed) return v;
            if ( state.backtracking==0 ) {
              v = LSVariable.nameVariable(CHK.reference((n!=null?n.getText():null),((funcblock_scope)funcblock_stack.peek()).func));
            }
            // Lua.g:76:3: ( '.' n2= NAME )*
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( (LA12_0==38) ) {
                    alt12=1;
                }


                switch (alt12) {
            	case 1 :
            	    // Lua.g:76:4: '.' n2= NAME
            	    {
            	    match(input,38,FOLLOW_38_in_funcname839); if (state.failed) return v;
            	    n2=(Token)match(input,NAME,FOLLOW_NAME_in_funcname843); if (state.failed) return v;
            	    if ( state.backtracking==0 ) {
            	      v = v.fieldVariable((n2!=null?n2.getText():null));
            	    }

            	    }
            	    break;

            	default :
            	    break loop12;
                }
            } while (true);

            // Lua.g:77:3: ( ':' n3= NAME )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==39) ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // Lua.g:77:4: ':' n3= NAME
                    {
                    match(input,39,FOLLOW_39_in_funcname854); if (state.failed) return v;
                    n3=(Token)match(input,NAME,FOLLOW_NAME_in_funcname858); if (state.failed) return v;
                    if ( state.backtracking==0 ) {
                      v = v.methodVariable((n3!=null?n3.getText():null));
                    }

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return v;
    }
    // $ANTLR end "funcname"


    // $ANTLR start "varlist1"
    // Lua.g:80:1: varlist1 returns [List<LSVariable> vars] : v1= var ( ',' v2= var )* ;
    public final List<LSVariable> varlist1() throws RecognitionException {
        List<LSVariable> vars = null;

        LSVariable v1 = null;

        LSVariable v2 = null;


         vars = new ArrayList<LSVariable>(); 
        try {
            // Lua.g:82:2: (v1= var ( ',' v2= var )* )
            // Lua.g:82:4: v1= var ( ',' v2= var )*
            {
            pushFollow(FOLLOW_var_in_varlist1887);
            v1=var();

            state._fsp--;
            if (state.failed) return vars;
            if ( state.backtracking==0 ) {
              vars.add(v1);
            }
            // Lua.g:83:3: ( ',' v2= var )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==28) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // Lua.g:83:4: ',' v2= var
            	    {
            	    match(input,28,FOLLOW_28_in_varlist1894); if (state.failed) return vars;
            	    pushFollow(FOLLOW_var_in_varlist1898);
            	    v2=var();

            	    state._fsp--;
            	    if (state.failed) return vars;
            	    if ( state.backtracking==0 ) {
            	      vars.add(v2);
            	    }

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return vars;
    }
    // $ANTLR end "varlist1"


    // $ANTLR start "namelist"
    // Lua.g:86:1: namelist returns [List<String> names] : n= NAME ( ',' n2= NAME )* ;
    public final List<String> namelist() throws RecognitionException {
        List<String> names = null;

        Token n=null;
        Token n2=null;

        try {
            // Lua.g:87:2: (n= NAME ( ',' n2= NAME )* )
            // Lua.g:87:4: n= NAME ( ',' n2= NAME )*
            {
            n=(Token)match(input,NAME,FOLLOW_NAME_in_namelist919); if (state.failed) return names;
            if ( state.backtracking==0 ) {
              names =new ArrayList<String>(); names.add((n!=null?n.getText():null));
            }
            // Lua.g:88:3: ( ',' n2= NAME )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==28) ) {
                    int LA15_2 = input.LA(2);

                    if ( (LA15_2==NAME) ) {
                        alt15=1;
                    }


                }


                switch (alt15) {
            	case 1 :
            	    // Lua.g:88:4: ',' n2= NAME
            	    {
            	    match(input,28,FOLLOW_28_in_namelist927); if (state.failed) return names;
            	    n2=(Token)match(input,NAME,FOLLOW_NAME_in_namelist931); if (state.failed) return names;
            	    if ( state.backtracking==0 ) {
            	      names.add((n2!=null?n2.getText():null));
            	    }

            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return names;
    }
    // $ANTLR end "namelist"


    // $ANTLR start "explist1"
    // Lua.g:91:1: explist1 returns [List<LSExpression> exprs] : (e1= exp ',' )* e2= exp ;
    public final List<LSExpression> explist1() throws RecognitionException {
        List<LSExpression> exprs = null;

        LSExpression e1 = null;

        LSExpression e2 = null;


         exprs = new ArrayList<LSExpression>(); 
        try {
            // Lua.g:93:2: ( (e1= exp ',' )* e2= exp )
            // Lua.g:93:4: (e1= exp ',' )* e2= exp
            {
            // Lua.g:93:4: (e1= exp ',' )*
            loop16:
            do {
                int alt16=2;
                alt16 = dfa16.predict(input);
                switch (alt16) {
            	case 1 :
            	    // Lua.g:93:5: e1= exp ','
            	    {
            	    pushFollow(FOLLOW_exp_in_explist1960);
            	    e1=exp();

            	    state._fsp--;
            	    if (state.failed) return exprs;
            	    match(input,28,FOLLOW_28_in_explist1962); if (state.failed) return exprs;
            	    if ( state.backtracking==0 ) {
            	      exprs.add(e1);
            	    }

            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);

            pushFollow(FOLLOW_exp_in_explist1973);
            e2=exp();

            state._fsp--;
            if (state.failed) return exprs;
            if ( state.backtracking==0 ) {
              exprs.add(e2);
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return exprs;
    }
    // $ANTLR end "explist1"


    // $ANTLR start "exp"
    // Lua.g:97:1: exp returns [LSExpression e] : ( 'nil' | 'false' | 'true' | number | string | '...' | function | prefixexp | tableconstructor | unop e1= exp ) ( binop e2= exp )* ;
    public final LSExpression exp() throws RecognitionException {
        LSExpression e = null;

        LSExpression e1 = null;

        LSExpression e2 = null;

        LuaParser.number_return number24 = null;

        LSExpression string25 = null;

        LSFunction function26 = null;

        LSVariable prefixexp27 = null;

        LSExpression tableconstructor28 = null;

        UnOp unop29 = null;

        BinOp binop30 = null;


        try {
            // Lua.g:98:2: ( ( 'nil' | 'false' | 'true' | number | string | '...' | function | prefixexp | tableconstructor | unop e1= exp ) ( binop e2= exp )* )
            // Lua.g:98:4: ( 'nil' | 'false' | 'true' | number | string | '...' | function | prefixexp | tableconstructor | unop e1= exp ) ( binop e2= exp )*
            {
            // Lua.g:98:4: ( 'nil' | 'false' | 'true' | number | string | '...' | function | prefixexp | tableconstructor | unop e1= exp )
            int alt17=10;
            alt17 = dfa17.predict(input);
            switch (alt17) {
                case 1 :
                    // Lua.g:98:5: 'nil'
                    {
                    match(input,40,FOLLOW_40_in_exp991); if (state.failed) return e;
                    if ( state.backtracking==0 ) {
                       e =LSExpression.ENIL; 
                    }

                    }
                    break;
                case 2 :
                    // Lua.g:99:5: 'false'
                    {
                    match(input,41,FOLLOW_41_in_exp1013); if (state.failed) return e;
                    if ( state.backtracking==0 ) {
                       e =LSExpression.EFALSE; 
                    }

                    }
                    break;
                case 3 :
                    // Lua.g:100:5: 'true'
                    {
                    match(input,42,FOLLOW_42_in_exp1030); if (state.failed) return e;
                    if ( state.backtracking==0 ) {
                       e =LSExpression.ETRUE; 
                    }

                    }
                    break;
                case 4 :
                    // Lua.g:101:5: number
                    {
                    pushFollow(FOLLOW_number_in_exp1048);
                    number24=number();

                    state._fsp--;
                    if (state.failed) return e;
                    if ( state.backtracking==0 ) {
                       e =LSExpression.numberExpression((number24!=null?input.toString(number24.start,number24.stop):null)); 
                    }

                    }
                    break;
                case 5 :
                    // Lua.g:102:5: string
                    {
                    pushFollow(FOLLOW_string_in_exp1066);
                    string25=string();

                    state._fsp--;
                    if (state.failed) return e;
                    if ( state.backtracking==0 ) {
                       e =string25; 
                    }

                    }
                    break;
                case 6 :
                    // Lua.g:103:5: '...'
                    {
                    match(input,43,FOLLOW_43_in_exp1084); if (state.failed) return e;
                    if ( state.backtracking==0 ) {
                       e =LSExpression.varargsRef(); ((funcblock_scope)funcblock_stack.peek()).func.setUsesVarargs(); 
                    }

                    }
                    break;
                case 7 :
                    // Lua.g:104:5: function
                    {
                    pushFollow(FOLLOW_function_in_exp1103);
                    function26=function();

                    state._fsp--;
                    if (state.failed) return e;
                    if ( state.backtracking==0 ) {
                       e =LSExpression.functionExpression(function26); 
                    }

                    }
                    break;
                case 8 :
                    // Lua.g:105:5: prefixexp
                    {
                    pushFollow(FOLLOW_prefixexp_in_exp1119);
                    prefixexp27=prefixexp();

                    state._fsp--;
                    if (state.failed) return e;
                    if ( state.backtracking==0 ) {
                       e =prefixexp27; 
                    }

                    }
                    break;
                case 9 :
                    // Lua.g:106:5: tableconstructor
                    {
                    pushFollow(FOLLOW_tableconstructor_in_exp1134);
                    tableconstructor28=tableconstructor();

                    state._fsp--;
                    if (state.failed) return e;
                    if ( state.backtracking==0 ) {
                       e =tableconstructor28;
                    }

                    }
                    break;
                case 10 :
                    // Lua.g:107:5: unop e1= exp
                    {
                    pushFollow(FOLLOW_unop_in_exp1142);
                    unop29=unop();

                    state._fsp--;
                    if (state.failed) return e;
                    pushFollow(FOLLOW_exp_in_exp1146);
                    e1=exp();

                    state._fsp--;
                    if (state.failed) return e;
                    if ( state.backtracking==0 ) {
                       e =LSExpression.unopExpression(unop29,e1,CHK.peekScope());
                    }

                    }
                    break;

            }

            // Lua.g:108:5: ( binop e2= exp )*
            loop18:
            do {
                int alt18=2;
                alt18 = dfa18.predict(input);
                switch (alt18) {
            	case 1 :
            	    // Lua.g:108:6: binop e2= exp
            	    {
            	    pushFollow(FOLLOW_binop_in_exp1160);
            	    binop30=binop();

            	    state._fsp--;
            	    if (state.failed) return e;
            	    pushFollow(FOLLOW_exp_in_exp1164);
            	    e2=exp();

            	    state._fsp--;
            	    if (state.failed) return e;
            	    if ( state.backtracking==0 ) {
            	       e =LSExpression.binopExpression(e,binop30,e2,CHK.peekScope());
            	    }

            	    }
            	    break;

            	default :
            	    break loop18;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return e;
    }
    // $ANTLR end "exp"

    protected static class var_scope {
        LSVariable current;
    }
    protected Stack var_stack = new Stack();


    // $ANTLR start "var"
    // Lua.g:111:1: var returns [LSVariable v] : (n= NAME | '(' exp ')' varSuffix ) ( varSuffix )* ;
    public final LSVariable var() throws RecognitionException {
        var_stack.push(new var_scope());
        LSVariable v = null;

        Token n=null;
        LSExpression exp31 = null;


        try {
            // Lua.g:113:2: ( (n= NAME | '(' exp ')' varSuffix ) ( varSuffix )* )
            // Lua.g:113:4: (n= NAME | '(' exp ')' varSuffix ) ( varSuffix )*
            {
            // Lua.g:113:4: (n= NAME | '(' exp ')' varSuffix )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==NAME) ) {
                alt19=1;
            }
            else if ( (LA19_0==44) ) {
                alt19=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return v;}
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }
            switch (alt19) {
                case 1 :
                    // Lua.g:113:5: n= NAME
                    {
                    n=(Token)match(input,NAME,FOLLOW_NAME_in_var1195); if (state.failed) return v;
                    if ( state.backtracking==0 ) {
                      ((var_scope)var_stack.peek()).current =LSVariable.nameVariable(CHK.reference((n!=null?n.getText():null),((funcblock_scope)funcblock_stack.peek()).func));
                    }

                    }
                    break;
                case 2 :
                    // Lua.g:114:8: '(' exp ')' varSuffix
                    {
                    match(input,44,FOLLOW_44_in_var1207); if (state.failed) return v;
                    pushFollow(FOLLOW_exp_in_var1209);
                    exp31=exp();

                    state._fsp--;
                    if (state.failed) return v;
                    match(input,45,FOLLOW_45_in_var1211); if (state.failed) return v;
                    if ( state.backtracking==0 ) {
                      ((var_scope)var_stack.peek()).current =LSVariable.parenthesesVariable(exp31);
                    }
                    pushFollow(FOLLOW_varSuffix_in_var1215);
                    varSuffix();

                    state._fsp--;
                    if (state.failed) return v;

                    }
                    break;

            }

            // Lua.g:115:7: ( varSuffix )*
            loop20:
            do {
                int alt20=2;
                alt20 = dfa20.predict(input);
                switch (alt20) {
            	case 1 :
            	    // Lua.g:0:0: varSuffix
            	    {
            	    pushFollow(FOLLOW_varSuffix_in_var1225);
            	    varSuffix();

            	    state._fsp--;
            	    if (state.failed) return v;

            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);

            if ( state.backtracking==0 ) {
              v =((var_scope)var_stack.peek()).current;
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            var_stack.pop();
        }
        return v;
    }
    // $ANTLR end "var"


    // $ANTLR start "varSuffix"
    // Lua.g:119:1: varSuffix : (n= nameAndArgs[$var::current] )* ( '[' e= exp ']' | '.' n2= NAME ) ;
    public final void varSuffix() throws RecognitionException {
        Token n2=null;
        LSVariable n = null;

        LSExpression e = null;


        try {
            // Lua.g:120:2: ( (n= nameAndArgs[$var::current] )* ( '[' e= exp ']' | '.' n2= NAME ) )
            // Lua.g:120:4: (n= nameAndArgs[$var::current] )* ( '[' e= exp ']' | '.' n2= NAME )
            {
            // Lua.g:120:4: (n= nameAndArgs[$var::current] )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( ((LA21_0>=NORMALSTRING && LA21_0<=LONGSTRING)||LA21_0==39||LA21_0==44||LA21_0==48) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // Lua.g:120:5: n= nameAndArgs[$var::current]
            	    {
            	    pushFollow(FOLLOW_nameAndArgs_in_varSuffix1245);
            	    n=nameAndArgs(((var_scope)var_stack.peek()).current);

            	    state._fsp--;
            	    if (state.failed) return ;
            	    if ( state.backtracking==0 ) {
            	      ((var_scope)var_stack.peek()).current =n;
            	    }

            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);

            // Lua.g:121:3: ( '[' e= exp ']' | '.' n2= NAME )
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==46) ) {
                alt22=1;
            }
            else if ( (LA22_0==38) ) {
                alt22=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }
            switch (alt22) {
                case 1 :
                    // Lua.g:121:4: '[' e= exp ']'
                    {
                    match(input,46,FOLLOW_46_in_varSuffix1256); if (state.failed) return ;
                    pushFollow(FOLLOW_exp_in_varSuffix1260);
                    e=exp();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,47,FOLLOW_47_in_varSuffix1262); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                      ((var_scope)var_stack.peek()).current =((var_scope)var_stack.peek()).current.indexVariable(e);
                    }

                    }
                    break;
                case 2 :
                    // Lua.g:122:5: '.' n2= NAME
                    {
                    match(input,38,FOLLOW_38_in_varSuffix1275); if (state.failed) return ;
                    n2=(Token)match(input,NAME,FOLLOW_NAME_in_varSuffix1279); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                      ((var_scope)var_stack.peek()).current =((var_scope)var_stack.peek()).current.fieldVariable((n2!=null?n2.getText():null));
                    }

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "varSuffix"

    protected static class prefixexp_scope {
        LSVariable current;
    }
    protected Stack prefixexp_stack = new Stack();


    // $ANTLR start "prefixexp"
    // Lua.g:126:1: prefixexp returns [LSVariable v] : e= varOrExp (n= nameAndArgs[$prefixexp::current] )* ;
    public final LSVariable prefixexp() throws RecognitionException {
        prefixexp_stack.push(new prefixexp_scope());
        LSVariable v = null;

        LSVariable e = null;

        LSVariable n = null;


        try {
            // Lua.g:128:2: (e= varOrExp (n= nameAndArgs[$prefixexp::current] )* )
            // Lua.g:128:4: e= varOrExp (n= nameAndArgs[$prefixexp::current] )*
            {
            pushFollow(FOLLOW_varOrExp_in_prefixexp1311);
            e=varOrExp();

            state._fsp--;
            if (state.failed) return v;
            if ( state.backtracking==0 ) {
              ((prefixexp_scope)prefixexp_stack.peek()).current =e;
            }
            // Lua.g:129:3: (n= nameAndArgs[$prefixexp::current] )*
            loop23:
            do {
                int alt23=2;
                alt23 = dfa23.predict(input);
                switch (alt23) {
            	case 1 :
            	    // Lua.g:129:4: n= nameAndArgs[$prefixexp::current]
            	    {
            	    pushFollow(FOLLOW_nameAndArgs_in_prefixexp1321);
            	    n=nameAndArgs(((prefixexp_scope)prefixexp_stack.peek()).current);

            	    state._fsp--;
            	    if (state.failed) return v;
            	    if ( state.backtracking==0 ) {
            	      ((prefixexp_scope)prefixexp_stack.peek()).current =n;
            	    }

            	    }
            	    break;

            	default :
            	    break loop23;
                }
            } while (true);

            if ( state.backtracking==0 ) {
              v =((prefixexp_scope)prefixexp_stack.peek()).current;
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            prefixexp_stack.pop();
        }
        return v;
    }
    // $ANTLR end "prefixexp"

    protected static class functioncall_scope {
        LSVariable current;
    }
    protected Stack functioncall_stack = new Stack();


    // $ANTLR start "functioncall"
    // Lua.g:133:1: functioncall returns [LSVariable v] : e= varOrExp (n= nameAndArgs[$functioncall::current] )+ ;
    public final LSVariable functioncall() throws RecognitionException {
        functioncall_stack.push(new functioncall_scope());
        LSVariable v = null;

        LSVariable e = null;

        LSVariable n = null;


        try {
            // Lua.g:135:2: (e= varOrExp (n= nameAndArgs[$functioncall::current] )+ )
            // Lua.g:135:4: e= varOrExp (n= nameAndArgs[$functioncall::current] )+
            {
            pushFollow(FOLLOW_varOrExp_in_functioncall1352);
            e=varOrExp();

            state._fsp--;
            if (state.failed) return v;
            if ( state.backtracking==0 ) {
              ((functioncall_scope)functioncall_stack.peek()).current =e;
            }
            // Lua.g:136:3: (n= nameAndArgs[$functioncall::current] )+
            int cnt24=0;
            loop24:
            do {
                int alt24=2;
                alt24 = dfa24.predict(input);
                switch (alt24) {
            	case 1 :
            	    // Lua.g:136:4: n= nameAndArgs[$functioncall::current]
            	    {
            	    pushFollow(FOLLOW_nameAndArgs_in_functioncall1362);
            	    n=nameAndArgs(((functioncall_scope)functioncall_stack.peek()).current);

            	    state._fsp--;
            	    if (state.failed) return v;
            	    if ( state.backtracking==0 ) {
            	      ((functioncall_scope)functioncall_stack.peek()).current =n;
            	    }

            	    }
            	    break;

            	default :
            	    if ( cnt24 >= 1 ) break loop24;
            	    if (state.backtracking>0) {state.failed=true; return v;}
                        EarlyExitException eee =
                            new EarlyExitException(24, input);
                        throw eee;
                }
                cnt24++;
            } while (true);

            if ( state.backtracking==0 ) {
              v =((functioncall_scope)functioncall_stack.peek()).current;
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            functioncall_stack.pop();
        }
        return v;
    }
    // $ANTLR end "functioncall"


    // $ANTLR start "varOrExp"
    // Lua.g:140:1: varOrExp returns [LSVariable v] : ( var | '(' exp ')' );
    public final LSVariable varOrExp() throws RecognitionException {
        LSVariable v = null;

        LSVariable var32 = null;

        LSExpression exp33 = null;


        try {
            // Lua.g:141:2: ( var | '(' exp ')' )
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==NAME) ) {
                alt25=1;
            }
            else if ( (LA25_0==44) ) {
                int LA25_2 = input.LA(2);

                if ( (synpred42_Lua()) ) {
                    alt25=1;
                }
                else if ( (true) ) {
                    alt25=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return v;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 25, 2, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return v;}
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;
            }
            switch (alt25) {
                case 1 :
                    // Lua.g:141:4: var
                    {
                    pushFollow(FOLLOW_var_in_varOrExp1386);
                    var32=var();

                    state._fsp--;
                    if (state.failed) return v;
                    if ( state.backtracking==0 ) {
                      v =var32;
                    }

                    }
                    break;
                case 2 :
                    // Lua.g:142:4: '(' exp ')'
                    {
                    match(input,44,FOLLOW_44_in_varOrExp1401); if (state.failed) return v;
                    pushFollow(FOLLOW_exp_in_varOrExp1403);
                    exp33=exp();

                    state._fsp--;
                    if (state.failed) return v;
                    match(input,45,FOLLOW_45_in_varOrExp1405); if (state.failed) return v;
                    if ( state.backtracking==0 ) {
                      v =LSVariable.parenthesesVariable(exp33);
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return v;
    }
    // $ANTLR end "varOrExp"


    // $ANTLR start "nameAndArgs"
    // Lua.g:145:1: nameAndArgs[LSVariable vin] returns [LSVariable v] : ( ':' n= NAME )? a= args ;
    public final LSVariable nameAndArgs(LSVariable vin) throws RecognitionException {
        LSVariable v = null;

        Token n=null;
        List<LSExpression> a = null;


         String method=null; 
        try {
            // Lua.g:147:2: ( ( ':' n= NAME )? a= args )
            // Lua.g:147:4: ( ':' n= NAME )? a= args
            {
            // Lua.g:147:4: ( ':' n= NAME )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==39) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // Lua.g:147:5: ':' n= NAME
                    {
                    match(input,39,FOLLOW_39_in_nameAndArgs1431); if (state.failed) return v;
                    n=(Token)match(input,NAME,FOLLOW_NAME_in_nameAndArgs1435); if (state.failed) return v;
                    if ( state.backtracking==0 ) {
                      method=(n!=null?n.getText():null);
                    }

                    }
                    break;

            }

            pushFollow(FOLLOW_args_in_nameAndArgs1452);
            a=args();

            state._fsp--;
            if (state.failed) return v;
            if ( state.backtracking==0 ) {
              v =((method==null)? 
              	       		vin.callFuncVariable(a):
              	       		vin.callMethVariable(method,a));
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return v;
    }
    // $ANTLR end "nameAndArgs"


    // $ANTLR start "args"
    // Lua.g:153:1: args returns [List<LSExpression> exprs] : ( '(' (e= explist1 )? ')' | t= tableconstructor | s= string );
    public final List<LSExpression> args() throws RecognitionException {
        List<LSExpression> exprs = null;

        List<LSExpression> e = null;

        LSExpression t = null;

        LSExpression s = null;


         exprs = new ArrayList<LSExpression>(); 
        try {
            // Lua.g:155:2: ( '(' (e= explist1 )? ')' | t= tableconstructor | s= string )
            int alt28=3;
            switch ( input.LA(1) ) {
            case 44:
                {
                alt28=1;
                }
                break;
            case 48:
                {
                alt28=2;
                }
                break;
            case NORMALSTRING:
            case CHARSTRING:
            case LONGSTRING:
                {
                alt28=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return exprs;}
                NoViableAltException nvae =
                    new NoViableAltException("", 28, 0, input);

                throw nvae;
            }

            switch (alt28) {
                case 1 :
                    // Lua.g:155:5: '(' (e= explist1 )? ')'
                    {
                    match(input,44,FOLLOW_44_in_args1476); if (state.failed) return exprs;
                    // Lua.g:155:9: (e= explist1 )?
                    int alt27=2;
                    int LA27_0 = input.LA(1);

                    if ( ((LA27_0>=NAME && LA27_0<=LONGSTRING)||LA27_0==30||(LA27_0>=40 && LA27_0<=44)||LA27_0==48||LA27_0==51||(LA27_0>=65 && LA27_0<=66)) ) {
                        alt27=1;
                    }
                    switch (alt27) {
                        case 1 :
                            // Lua.g:155:10: e= explist1
                            {
                            pushFollow(FOLLOW_explist1_in_args1481);
                            e=explist1();

                            state._fsp--;
                            if (state.failed) return exprs;
                            if ( state.backtracking==0 ) {
                              exprs =e;
                            }

                            }
                            break;

                    }

                    match(input,45,FOLLOW_45_in_args1487); if (state.failed) return exprs;

                    }
                    break;
                case 2 :
                    // Lua.g:156:4: t= tableconstructor
                    {
                    pushFollow(FOLLOW_tableconstructor_in_args1495);
                    t=tableconstructor();

                    state._fsp--;
                    if (state.failed) return exprs;
                    if ( state.backtracking==0 ) {
                      exprs.add(t);
                    }

                    }
                    break;
                case 3 :
                    // Lua.g:157:4: s= string
                    {
                    pushFollow(FOLLOW_string_in_args1505);
                    s=string();

                    state._fsp--;
                    if (state.failed) return exprs;
                    if ( state.backtracking==0 ) {
                      exprs.add(s);
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return exprs;
    }
    // $ANTLR end "args"


    // $ANTLR start "function"
    // Lua.g:160:1: function returns [LSFunction f] : 'function' b= funcbody ;
    public final LSFunction function() throws RecognitionException {
        LSFunction f = null;

        LSFunction b = null;


        try {
            // Lua.g:161:2: ( 'function' b= funcbody )
            // Lua.g:161:4: 'function' b= funcbody
            {
            match(input,30,FOLLOW_30_in_function1532); if (state.failed) return f;
            pushFollow(FOLLOW_funcbody_in_function1536);
            b=funcbody();

            state._fsp--;
            if (state.failed) return f;
            if ( state.backtracking==0 ) {
              f = b;
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return f;
    }
    // $ANTLR end "function"


    // $ANTLR start "funcbody"
    // Lua.g:164:1: funcbody returns [LSFunction f] : '(' ( parlist1[f] )? ')' funcblock[f] 'end' ;
    public final LSFunction funcbody() throws RecognitionException {
        LSFunction f = null;

         
        		f = new LSFunction();
        		((funcblock_scope)funcblock_stack.peek()).func.functions.add(f);
        	
        try {
            // Lua.g:169:2: ( '(' ( parlist1[f] )? ')' funcblock[f] 'end' )
            // Lua.g:169:4: '(' ( parlist1[f] )? ')' funcblock[f] 'end'
            {
            if ( state.backtracking==0 ) {
              CHK.pushScope("func",true);
            }
            match(input,44,FOLLOW_44_in_funcbody1561); if (state.failed) return f;
            // Lua.g:169:38: ( parlist1[f] )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==NAME||LA29_0==43) ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // Lua.g:169:39: parlist1[f]
                    {
                    pushFollow(FOLLOW_parlist1_in_funcbody1564);
                    parlist1(f);

                    state._fsp--;
                    if (state.failed) return f;

                    }
                    break;

            }

            match(input,45,FOLLOW_45_in_funcbody1570); if (state.failed) return f;
            pushFollow(FOLLOW_funcblock_in_funcbody1572);
            funcblock(f);

            state._fsp--;
            if (state.failed) return f;
            match(input,23,FOLLOW_23_in_funcbody1575); if (state.failed) return f;
            if ( state.backtracking==0 ) {
              CHK.popScope("func");
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return f;
    }
    // $ANTLR end "funcbody"


    // $ANTLR start "parlist1"
    // Lua.g:172:1: parlist1[LSFunction f] : ( namelist ( ',' '...' )? | '...' );
    public final void parlist1(LSFunction f) throws RecognitionException {
        List<String> namelist34 = null;


        try {
            // Lua.g:173:2: ( namelist ( ',' '...' )? | '...' )
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==NAME) ) {
                alt31=1;
            }
            else if ( (LA31_0==43) ) {
                alt31=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;
            }
            switch (alt31) {
                case 1 :
                    // Lua.g:173:4: namelist ( ',' '...' )?
                    {
                    pushFollow(FOLLOW_namelist_in_parlist11591);
                    namelist34=namelist();

                    state._fsp--;
                    if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                      f.setParameterNames(CHK.declare(namelist34));
                    }
                    // Lua.g:173:66: ( ',' '...' )?
                    int alt30=2;
                    int LA30_0 = input.LA(1);

                    if ( (LA30_0==28) ) {
                        alt30=1;
                    }
                    switch (alt30) {
                        case 1 :
                            // Lua.g:173:67: ',' '...'
                            {
                            match(input,28,FOLLOW_28_in_parlist11596); if (state.failed) return ;
                            match(input,43,FOLLOW_43_in_parlist11598); if (state.failed) return ;
                            if ( state.backtracking==0 ) {
                              f.isvararg=true;
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // Lua.g:174:4: '...'
                    {
                    match(input,43,FOLLOW_43_in_parlist11608); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                      f.isvararg=true;
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "parlist1"


    // $ANTLR start "tableconstructor"
    // Lua.g:177:1: tableconstructor returns [LSExpression e] : '{' ( fieldlist[fields] )? '}' ;
    public final LSExpression tableconstructor() throws RecognitionException {
        LSExpression e = null;

         List<LSField> fields = new ArrayList<LSField>(); 
        try {
            // Lua.g:179:2: ( '{' ( fieldlist[fields] )? '}' )
            // Lua.g:179:4: '{' ( fieldlist[fields] )? '}'
            {
            match(input,48,FOLLOW_48_in_tableconstructor1631); if (state.failed) return e;
            // Lua.g:179:8: ( fieldlist[fields] )?
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( ((LA32_0>=NAME && LA32_0<=LONGSTRING)||LA32_0==30||(LA32_0>=40 && LA32_0<=44)||LA32_0==46||LA32_0==48||LA32_0==51||(LA32_0>=65 && LA32_0<=66)) ) {
                alt32=1;
            }
            switch (alt32) {
                case 1 :
                    // Lua.g:179:9: fieldlist[fields]
                    {
                    pushFollow(FOLLOW_fieldlist_in_tableconstructor1634);
                    fieldlist(fields);

                    state._fsp--;
                    if (state.failed) return e;

                    }
                    break;

            }

            match(input,49,FOLLOW_49_in_tableconstructor1639); if (state.failed) return e;
            if ( state.backtracking==0 ) {
              e =LSExpression.tableConstructorExpression(fields);
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return e;
    }
    // $ANTLR end "tableconstructor"


    // $ANTLR start "fieldlist"
    // Lua.g:182:1: fieldlist[List<LSField> fields] : field[fields] ( fieldsep field[fields] )* ( fieldsep )? ;
    public final void fieldlist(List<LSField> fields) throws RecognitionException {
        try {
            // Lua.g:183:2: ( field[fields] ( fieldsep field[fields] )* ( fieldsep )? )
            // Lua.g:183:4: field[fields] ( fieldsep field[fields] )* ( fieldsep )?
            {
            pushFollow(FOLLOW_field_in_fieldlist1654);
            field(fields);

            state._fsp--;
            if (state.failed) return ;
            // Lua.g:183:19: ( fieldsep field[fields] )*
            loop33:
            do {
                int alt33=2;
                int LA33_0 = input.LA(1);

                if ( (LA33_0==20||LA33_0==28) ) {
                    int LA33_1 = input.LA(2);

                    if ( ((LA33_1>=NAME && LA33_1<=LONGSTRING)||LA33_1==30||(LA33_1>=40 && LA33_1<=44)||LA33_1==46||LA33_1==48||LA33_1==51||(LA33_1>=65 && LA33_1<=66)) ) {
                        alt33=1;
                    }


                }


                switch (alt33) {
            	case 1 :
            	    // Lua.g:183:20: fieldsep field[fields]
            	    {
            	    pushFollow(FOLLOW_fieldsep_in_fieldlist1659);
            	    fieldsep();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    pushFollow(FOLLOW_field_in_fieldlist1661);
            	    field(fields);

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop33;
                }
            } while (true);

            // Lua.g:183:46: ( fieldsep )?
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==20||LA34_0==28) ) {
                alt34=1;
            }
            switch (alt34) {
                case 1 :
                    // Lua.g:183:47: fieldsep
                    {
                    pushFollow(FOLLOW_fieldsep_in_fieldlist1668);
                    fieldsep();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "fieldlist"


    // $ANTLR start "field"
    // Lua.g:186:1: field[List<LSField> fields] : ( '[' k= exp ']' '=' e= exp | n= NAME '=' e= exp | e= exp );
    public final void field(List<LSField> fields) throws RecognitionException {
        Token n=null;
        LSExpression k = null;

        LSExpression e = null;


        try {
            // Lua.g:187:2: ( '[' k= exp ']' '=' e= exp | n= NAME '=' e= exp | e= exp )
            int alt35=3;
            switch ( input.LA(1) ) {
            case 46:
                {
                alt35=1;
                }
                break;
            case NAME:
                {
                int LA35_2 = input.LA(2);

                if ( (LA35_2==21) ) {
                    alt35=2;
                }
                else if ( (LA35_2==EOF||(LA35_2>=NORMALSTRING && LA35_2<=LONGSTRING)||LA35_2==20||LA35_2==28||(LA35_2>=38 && LA35_2<=39)||LA35_2==44||LA35_2==46||(LA35_2>=48 && LA35_2<=64)) ) {
                    alt35=3;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 35, 2, input);

                    throw nvae;
                }
                }
                break;
            case INT:
            case FLOAT1:
            case FLOAT2:
            case FLOAT3:
            case EXP:
            case HEX:
            case NORMALSTRING:
            case CHARSTRING:
            case LONGSTRING:
            case 30:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 48:
            case 51:
            case 65:
            case 66:
                {
                alt35=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 35, 0, input);

                throw nvae;
            }

            switch (alt35) {
                case 1 :
                    // Lua.g:187:4: '[' k= exp ']' '=' e= exp
                    {
                    match(input,46,FOLLOW_46_in_field1683); if (state.failed) return ;
                    pushFollow(FOLLOW_exp_in_field1687);
                    k=exp();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,47,FOLLOW_47_in_field1689); if (state.failed) return ;
                    match(input,21,FOLLOW_21_in_field1691); if (state.failed) return ;
                    pushFollow(FOLLOW_exp_in_field1695);
                    e=exp();

                    state._fsp--;
                    if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                      fields.add(LSField.keyValueField(k,e));
                    }

                    }
                    break;
                case 2 :
                    // Lua.g:188:4: n= NAME '=' e= exp
                    {
                    n=(Token)match(input,NAME,FOLLOW_NAME_in_field1706); if (state.failed) return ;
                    match(input,21,FOLLOW_21_in_field1708); if (state.failed) return ;
                    pushFollow(FOLLOW_exp_in_field1712);
                    e=exp();

                    state._fsp--;
                    if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                      fields.add(LSField.nameValueField((n!=null?n.getText():null),e));
                    }

                    }
                    break;
                case 3 :
                    // Lua.g:189:4: e= exp
                    {
                    pushFollow(FOLLOW_exp_in_field1728);
                    e=exp();

                    state._fsp--;
                    if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                      fields.add(LSField.valueField(e));
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "field"


    // $ANTLR start "fieldsep"
    // Lua.g:192:1: fieldsep : ( ',' | ';' );
    public final void fieldsep() throws RecognitionException {
        try {
            // Lua.g:193:2: ( ',' | ';' )
            // Lua.g:
            {
            if ( input.LA(1)==20||input.LA(1)==28 ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "fieldsep"


    // $ANTLR start "binop"
    // Lua.g:197:1: binop returns [BinOp op] : ( '+' | '-' | '*' | '/' | '^' | '%' | '..' | '<' | '<=' | '>' | '>=' | '==' | '~=' | 'and' | 'or' );
    public final BinOp binop() throws RecognitionException {
        BinOp op = null;

        try {
            // Lua.g:198:2: ( '+' | '-' | '*' | '/' | '^' | '%' | '..' | '<' | '<=' | '>' | '>=' | '==' | '~=' | 'and' | 'or' )
            int alt36=15;
            switch ( input.LA(1) ) {
            case 50:
                {
                alt36=1;
                }
                break;
            case 51:
                {
                alt36=2;
                }
                break;
            case 52:
                {
                alt36=3;
                }
                break;
            case 53:
                {
                alt36=4;
                }
                break;
            case 54:
                {
                alt36=5;
                }
                break;
            case 55:
                {
                alt36=6;
                }
                break;
            case 56:
                {
                alt36=7;
                }
                break;
            case 57:
                {
                alt36=8;
                }
                break;
            case 58:
                {
                alt36=9;
                }
                break;
            case 59:
                {
                alt36=10;
                }
                break;
            case 60:
                {
                alt36=11;
                }
                break;
            case 61:
                {
                alt36=12;
                }
                break;
            case 62:
                {
                alt36=13;
                }
                break;
            case 63:
                {
                alt36=14;
                }
                break;
            case 64:
                {
                alt36=15;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return op;}
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;
            }

            switch (alt36) {
                case 1 :
                    // Lua.g:198:4: '+'
                    {
                    match(input,50,FOLLOW_50_in_binop1781); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =BinOp.ADD;
                    }

                    }
                    break;
                case 2 :
                    // Lua.g:199:4: '-'
                    {
                    match(input,51,FOLLOW_51_in_binop1790); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =BinOp.SUB;
                    }

                    }
                    break;
                case 3 :
                    // Lua.g:200:4: '*'
                    {
                    match(input,52,FOLLOW_52_in_binop1800); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =BinOp.MUL;
                    }

                    }
                    break;
                case 4 :
                    // Lua.g:201:4: '/'
                    {
                    match(input,53,FOLLOW_53_in_binop1810); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =BinOp.DIV;
                    }

                    }
                    break;
                case 5 :
                    // Lua.g:202:4: '^'
                    {
                    match(input,54,FOLLOW_54_in_binop1820); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =BinOp.POW;
                    }

                    }
                    break;
                case 6 :
                    // Lua.g:203:4: '%'
                    {
                    match(input,55,FOLLOW_55_in_binop1830); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =BinOp.MOD;
                    }

                    }
                    break;
                case 7 :
                    // Lua.g:204:4: '..'
                    {
                    match(input,56,FOLLOW_56_in_binop1840); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =BinOp.CONCAT;
                    }

                    }
                    break;
                case 8 :
                    // Lua.g:205:4: '<'
                    {
                    match(input,57,FOLLOW_57_in_binop1849); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =BinOp.LT;
                    }

                    }
                    break;
                case 9 :
                    // Lua.g:206:4: '<='
                    {
                    match(input,58,FOLLOW_58_in_binop1858); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =BinOp.LTEQ;
                    }

                    }
                    break;
                case 10 :
                    // Lua.g:207:4: '>'
                    {
                    match(input,59,FOLLOW_59_in_binop1867); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =BinOp.GT;
                    }

                    }
                    break;
                case 11 :
                    // Lua.g:208:4: '>='
                    {
                    match(input,60,FOLLOW_60_in_binop1876); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =BinOp.GTEQ;
                    }

                    }
                    break;
                case 12 :
                    // Lua.g:209:4: '=='
                    {
                    match(input,61,FOLLOW_61_in_binop1885); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =BinOp.EQ;
                    }

                    }
                    break;
                case 13 :
                    // Lua.g:210:4: '~='
                    {
                    match(input,62,FOLLOW_62_in_binop1894); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =BinOp.NEQ;
                    }

                    }
                    break;
                case 14 :
                    // Lua.g:211:4: 'and'
                    {
                    match(input,63,FOLLOW_63_in_binop1903); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =BinOp.AND; ((funcblock_scope)funcblock_stack.peek()).func.hasandlogic=true;
                    }

                    }
                    break;
                case 15 :
                    // Lua.g:212:4: 'or'
                    {
                    match(input,64,FOLLOW_64_in_binop1911); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =BinOp.OR;  ((funcblock_scope)funcblock_stack.peek()).func.hasorlogic=true;
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return op;
    }
    // $ANTLR end "binop"


    // $ANTLR start "unop"
    // Lua.g:215:1: unop returns [UnOp op] : ( '-' | 'not' | '#' );
    public final UnOp unop() throws RecognitionException {
        UnOp op = null;

        try {
            // Lua.g:216:2: ( '-' | 'not' | '#' )
            int alt37=3;
            switch ( input.LA(1) ) {
            case 51:
                {
                alt37=1;
                }
                break;
            case 65:
                {
                alt37=2;
                }
                break;
            case 66:
                {
                alt37=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return op;}
                NoViableAltException nvae =
                    new NoViableAltException("", 37, 0, input);

                throw nvae;
            }

            switch (alt37) {
                case 1 :
                    // Lua.g:216:4: '-'
                    {
                    match(input,51,FOLLOW_51_in_unop1930); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =UnOp.NEG;
                    }

                    }
                    break;
                case 2 :
                    // Lua.g:217:4: 'not'
                    {
                    match(input,65,FOLLOW_65_in_unop1940); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =UnOp.NOT;
                    }

                    }
                    break;
                case 3 :
                    // Lua.g:218:4: '#'
                    {
                    match(input,66,FOLLOW_66_in_unop1949); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                      op =UnOp.LEN;
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return op;
    }
    // $ANTLR end "unop"

    public static class number_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "number"
    // Lua.g:221:1: number : ( ( '-' )? INT | ( '-' )? FLOAT1 | ( '-' )? FLOAT2 | ( '-' )? FLOAT3 | ( '-' )? EXP | HEX );
    public final LuaParser.number_return number() throws RecognitionException {
        LuaParser.number_return retval = new LuaParser.number_return();
        retval.start = input.LT(1);

        try {
            // Lua.g:222:2: ( ( '-' )? INT | ( '-' )? FLOAT1 | ( '-' )? FLOAT2 | ( '-' )? FLOAT3 | ( '-' )? EXP | HEX )
            int alt43=6;
            switch ( input.LA(1) ) {
            case 51:
                {
                switch ( input.LA(2) ) {
                case EXP:
                    {
                    alt43=5;
                    }
                    break;
                case FLOAT1:
                    {
                    alt43=2;
                    }
                    break;
                case FLOAT3:
                    {
                    alt43=4;
                    }
                    break;
                case FLOAT2:
                    {
                    alt43=3;
                    }
                    break;
                case INT:
                    {
                    alt43=1;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 43, 1, input);

                    throw nvae;
                }

                }
                break;
            case INT:
                {
                alt43=1;
                }
                break;
            case FLOAT1:
                {
                alt43=2;
                }
                break;
            case FLOAT2:
                {
                alt43=3;
                }
                break;
            case FLOAT3:
                {
                alt43=4;
                }
                break;
            case EXP:
                {
                alt43=5;
                }
                break;
            case HEX:
                {
                alt43=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                throw nvae;
            }

            switch (alt43) {
                case 1 :
                    // Lua.g:222:4: ( '-' )? INT
                    {
                    // Lua.g:222:4: ( '-' )?
                    int alt38=2;
                    int LA38_0 = input.LA(1);

                    if ( (LA38_0==51) ) {
                        alt38=1;
                    }
                    switch (alt38) {
                        case 1 :
                            // Lua.g:222:5: '-'
                            {
                            match(input,51,FOLLOW_51_in_number1967); if (state.failed) return retval;

                            }
                            break;

                    }

                    match(input,INT,FOLLOW_INT_in_number1971); if (state.failed) return retval;

                    }
                    break;
                case 2 :
                    // Lua.g:223:4: ( '-' )? FLOAT1
                    {
                    // Lua.g:223:4: ( '-' )?
                    int alt39=2;
                    int LA39_0 = input.LA(1);

                    if ( (LA39_0==51) ) {
                        alt39=1;
                    }
                    switch (alt39) {
                        case 1 :
                            // Lua.g:223:5: '-'
                            {
                            match(input,51,FOLLOW_51_in_number1978); if (state.failed) return retval;

                            }
                            break;

                    }

                    match(input,FLOAT1,FOLLOW_FLOAT1_in_number1982); if (state.failed) return retval;

                    }
                    break;
                case 3 :
                    // Lua.g:224:4: ( '-' )? FLOAT2
                    {
                    // Lua.g:224:4: ( '-' )?
                    int alt40=2;
                    int LA40_0 = input.LA(1);

                    if ( (LA40_0==51) ) {
                        alt40=1;
                    }
                    switch (alt40) {
                        case 1 :
                            // Lua.g:224:5: '-'
                            {
                            match(input,51,FOLLOW_51_in_number1989); if (state.failed) return retval;

                            }
                            break;

                    }

                    match(input,FLOAT2,FOLLOW_FLOAT2_in_number1993); if (state.failed) return retval;

                    }
                    break;
                case 4 :
                    // Lua.g:225:4: ( '-' )? FLOAT3
                    {
                    // Lua.g:225:4: ( '-' )?
                    int alt41=2;
                    int LA41_0 = input.LA(1);

                    if ( (LA41_0==51) ) {
                        alt41=1;
                    }
                    switch (alt41) {
                        case 1 :
                            // Lua.g:225:5: '-'
                            {
                            match(input,51,FOLLOW_51_in_number2000); if (state.failed) return retval;

                            }
                            break;

                    }

                    match(input,FLOAT3,FOLLOW_FLOAT3_in_number2004); if (state.failed) return retval;

                    }
                    break;
                case 5 :
                    // Lua.g:226:4: ( '-' )? EXP
                    {
                    // Lua.g:226:4: ( '-' )?
                    int alt42=2;
                    int LA42_0 = input.LA(1);

                    if ( (LA42_0==51) ) {
                        alt42=1;
                    }
                    switch (alt42) {
                        case 1 :
                            // Lua.g:226:5: '-'
                            {
                            match(input,51,FOLLOW_51_in_number2011); if (state.failed) return retval;

                            }
                            break;

                    }

                    match(input,EXP,FOLLOW_EXP_in_number2015); if (state.failed) return retval;

                    }
                    break;
                case 6 :
                    // Lua.g:227:4: HEX
                    {
                    match(input,HEX,FOLLOW_HEX_in_number2021); if (state.failed) return retval;

                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "number"


    // $ANTLR start "string"
    // Lua.g:230:1: string returns [LSExpression e] : ( NORMALSTRING | CHARSTRING | LONGSTRING );
    public final LSExpression string() throws RecognitionException {
        LSExpression e = null;

        Token NORMALSTRING35=null;
        Token CHARSTRING36=null;
        Token LONGSTRING37=null;

        try {
            // Lua.g:231:2: ( NORMALSTRING | CHARSTRING | LONGSTRING )
            int alt44=3;
            switch ( input.LA(1) ) {
            case NORMALSTRING:
                {
                alt44=1;
                }
                break;
            case CHARSTRING:
                {
                alt44=2;
                }
                break;
            case LONGSTRING:
                {
                alt44=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return e;}
                NoViableAltException nvae =
                    new NoViableAltException("", 44, 0, input);

                throw nvae;
            }

            switch (alt44) {
                case 1 :
                    // Lua.g:231:4: NORMALSTRING
                    {
                    NORMALSTRING35=(Token)match(input,NORMALSTRING,FOLLOW_NORMALSTRING_in_string2037); if (state.failed) return e;
                    if ( state.backtracking==0 ) {
                      e =LSExpression.normalStringExpression((NORMALSTRING35!=null?NORMALSTRING35.getText():null));
                    }

                    }
                    break;
                case 2 :
                    // Lua.g:232:4: CHARSTRING
                    {
                    CHARSTRING36=(Token)match(input,CHARSTRING,FOLLOW_CHARSTRING_in_string2044); if (state.failed) return e;
                    if ( state.backtracking==0 ) {
                      e =LSExpression.charStringExpression((CHARSTRING36!=null?CHARSTRING36.getText():null));
                    }

                    }
                    break;
                case 3 :
                    // Lua.g:233:4: LONGSTRING
                    {
                    LONGSTRING37=(Token)match(input,LONGSTRING,FOLLOW_LONGSTRING_in_string2053); if (state.failed) return e;
                    if ( state.backtracking==0 ) {
                      e =LSExpression.longStringExpression((LONGSTRING37!=null?LONGSTRING37.getText():null));
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return e;
    }
    // $ANTLR end "string"

    // $ANTLR start synpred5_Lua
    public final void synpred5_Lua_fragment() throws RecognitionException {   
        // Lua.g:41:4: ( varlist1 '=' explist1 )
        // Lua.g:41:4: varlist1 '=' explist1
        {
        pushFollow(FOLLOW_varlist1_in_synpred5_Lua159);
        varlist1();

        state._fsp--;
        if (state.failed) return ;
        match(input,21,FOLLOW_21_in_synpred5_Lua161); if (state.failed) return ;
        pushFollow(FOLLOW_explist1_in_synpred5_Lua163);
        explist1();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Lua

    // $ANTLR start synpred6_Lua
    public final void synpred6_Lua_fragment() throws RecognitionException {   
        // Lua.g:42:4: ( functioncall )
        // Lua.g:42:4: functioncall
        {
        pushFollow(FOLLOW_functioncall_in_synpred6_Lua183);
        functioncall();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Lua

    // $ANTLR start synpred12_Lua
    public final void synpred12_Lua_fragment() throws RecognitionException {   
        LSExpression e1 = null;

        LSExpression e2 = null;

        LSExpression e3 = null;


        // Lua.g:50:4: ( 'for' NAME '=' e1= exp ',' e2= exp ( ',' e3= exp )? 'do' block 'end' )
        // Lua.g:50:4: 'for' NAME '=' e1= exp ',' e2= exp ( ',' e3= exp )? 'do' block 'end'
        {
        match(input,27,FOLLOW_27_in_synpred12_Lua445); if (state.failed) return ;
        match(input,NAME,FOLLOW_NAME_in_synpred12_Lua449); if (state.failed) return ;
        match(input,21,FOLLOW_21_in_synpred12_Lua453); if (state.failed) return ;
        pushFollow(FOLLOW_exp_in_synpred12_Lua457);
        e1=exp();

        state._fsp--;
        if (state.failed) return ;
        match(input,28,FOLLOW_28_in_synpred12_Lua459); if (state.failed) return ;
        pushFollow(FOLLOW_exp_in_synpred12_Lua463);
        e2=exp();

        state._fsp--;
        if (state.failed) return ;
        // Lua.g:50:94: ( ',' e3= exp )?
        int alt47=2;
        int LA47_0 = input.LA(1);

        if ( (LA47_0==28) ) {
            alt47=1;
        }
        switch (alt47) {
            case 1 :
                // Lua.g:50:95: ',' e3= exp
                {
                match(input,28,FOLLOW_28_in_synpred12_Lua466); if (state.failed) return ;
                pushFollow(FOLLOW_exp_in_synpred12_Lua470);
                e3=exp();

                state._fsp--;
                if (state.failed) return ;

                }
                break;

        }

        match(input,22,FOLLOW_22_in_synpred12_Lua474); if (state.failed) return ;
        pushFollow(FOLLOW_block_in_synpred12_Lua478);
        block();

        state._fsp--;
        if (state.failed) return ;
        match(input,23,FOLLOW_23_in_synpred12_Lua482); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_Lua

    // $ANTLR start synpred13_Lua
    public final void synpred13_Lua_fragment() throws RecognitionException {   
        // Lua.g:52:4: ( 'for' namelist 'in' explist1 'do' block 'end' )
        // Lua.g:52:4: 'for' namelist 'in' explist1 'do' block 'end'
        {
        match(input,27,FOLLOW_27_in_synpred13_Lua528); if (state.failed) return ;
        pushFollow(FOLLOW_namelist_in_synpred13_Lua533);
        namelist();

        state._fsp--;
        if (state.failed) return ;
        match(input,29,FOLLOW_29_in_synpred13_Lua537); if (state.failed) return ;
        pushFollow(FOLLOW_explist1_in_synpred13_Lua539);
        explist1();

        state._fsp--;
        if (state.failed) return ;
        match(input,22,FOLLOW_22_in_synpred13_Lua541); if (state.failed) return ;
        pushFollow(FOLLOW_block_in_synpred13_Lua545);
        block();

        state._fsp--;
        if (state.failed) return ;
        match(input,23,FOLLOW_23_in_synpred13_Lua548); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_Lua

    // $ANTLR start synpred15_Lua
    public final void synpred15_Lua_fragment() throws RecognitionException {   
        // Lua.g:55:4: ( 'local' 'function' NAME funcbody )
        // Lua.g:55:4: 'local' 'function' NAME funcbody
        {
        match(input,31,FOLLOW_31_in_synpred15_Lua610); if (state.failed) return ;
        match(input,30,FOLLOW_30_in_synpred15_Lua612); if (state.failed) return ;
        match(input,NAME,FOLLOW_NAME_in_synpred15_Lua614); if (state.failed) return ;
        pushFollow(FOLLOW_funcbody_in_synpred15_Lua618);
        funcbody();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred15_Lua

    // $ANTLR start synpred25_Lua
    public final void synpred25_Lua_fragment() throws RecognitionException {   
        LSExpression e1 = null;


        // Lua.g:93:5: (e1= exp ',' )
        // Lua.g:93:5: e1= exp ','
        {
        pushFollow(FOLLOW_exp_in_synpred25_Lua960);
        e1=exp();

        state._fsp--;
        if (state.failed) return ;
        match(input,28,FOLLOW_28_in_synpred25_Lua962); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred25_Lua

    // $ANTLR start synpred29_Lua
    public final void synpred29_Lua_fragment() throws RecognitionException {   
        // Lua.g:101:5: ( number )
        // Lua.g:101:5: number
        {
        pushFollow(FOLLOW_number_in_synpred29_Lua1048);
        number();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred29_Lua

    // $ANTLR start synpred35_Lua
    public final void synpred35_Lua_fragment() throws RecognitionException {   
        LSExpression e2 = null;


        // Lua.g:108:6: ( binop e2= exp )
        // Lua.g:108:6: binop e2= exp
        {
        pushFollow(FOLLOW_binop_in_synpred35_Lua1160);
        binop();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_exp_in_synpred35_Lua1164);
        e2=exp();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred35_Lua

    // $ANTLR start synpred37_Lua
    public final void synpred37_Lua_fragment() throws RecognitionException {   
        // Lua.g:115:7: ( varSuffix )
        // Lua.g:115:7: varSuffix
        {
        pushFollow(FOLLOW_varSuffix_in_synpred37_Lua1225);
        varSuffix();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred37_Lua

    // $ANTLR start synpred40_Lua
    public final void synpred40_Lua_fragment() throws RecognitionException {   
        LSVariable n = null;


        // Lua.g:129:4: (n= nameAndArgs[$prefixexp::current] )
        // Lua.g:129:4: n= nameAndArgs[$prefixexp::current]
        {
        pushFollow(FOLLOW_nameAndArgs_in_synpred40_Lua1321);
        n=nameAndArgs(((prefixexp_scope)prefixexp_stack.peek()).current);

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred40_Lua

    // $ANTLR start synpred41_Lua
    public final void synpred41_Lua_fragment() throws RecognitionException {   
        LSVariable n = null;


        // Lua.g:136:4: (n= nameAndArgs[$functioncall::current] )
        // Lua.g:136:4: n= nameAndArgs[$functioncall::current]
        {
        pushFollow(FOLLOW_nameAndArgs_in_synpred41_Lua1362);
        n=nameAndArgs(((functioncall_scope)functioncall_stack.peek()).current);

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred41_Lua

    // $ANTLR start synpred42_Lua
    public final void synpred42_Lua_fragment() throws RecognitionException {   
        // Lua.g:141:4: ( var )
        // Lua.g:141:4: var
        {
        pushFollow(FOLLOW_var_in_synpred42_Lua1386);
        var();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred42_Lua

    // Delegated rules

    public final boolean synpred25_Lua() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred25_Lua_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred41_Lua() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred41_Lua_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred40_Lua() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred40_Lua_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred42_Lua() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred42_Lua_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred13_Lua() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred13_Lua_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred35_Lua() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred35_Lua_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred5_Lua() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred5_Lua_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred29_Lua() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred29_Lua_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred12_Lua() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred12_Lua_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred15_Lua() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred15_Lua_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred6_Lua() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred6_Lua_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred37_Lua() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred37_Lua_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA7 dfa7 = new DFA7(this);
    protected DFA16 dfa16 = new DFA16(this);
    protected DFA17 dfa17 = new DFA17(this);
    protected DFA18 dfa18 = new DFA18(this);
    protected DFA20 dfa20 = new DFA20(this);
    protected DFA23 dfa23 = new DFA23(this);
    protected DFA24 dfa24 = new DFA24(this);
    static final String DFA7_eotS =
        "\20\uffff";
    static final String DFA7_eofS =
        "\20\uffff";
    static final String DFA7_minS =
        "\1\4\2\0\4\uffff\1\0\1\uffff\1\0\6\uffff";
    static final String DFA7_maxS =
        "\1\54\2\0\4\uffff\1\0\1\uffff\1\0\6\uffff";
    static final String DFA7_acceptS =
        "\3\uffff\1\3\1\4\1\5\1\6\1\uffff\1\11\1\uffff\1\1\1\2\1\7\1\10"+
        "\1\12\1\13";
    static final String DFA7_specialS =
        "\1\uffff\1\0\1\1\4\uffff\1\2\1\uffff\1\3\6\uffff}>";
    static final String[] DFA7_transitionS = {
            "\1\1\21\uffff\1\3\1\uffff\1\4\1\5\1\uffff\1\7\2\uffff\1\10"+
            "\1\11\1\6\13\uffff\1\2",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA7_eot = DFA.unpackEncodedString(DFA7_eotS);
    static final short[] DFA7_eof = DFA.unpackEncodedString(DFA7_eofS);
    static final char[] DFA7_min = DFA.unpackEncodedStringToUnsignedChars(DFA7_minS);
    static final char[] DFA7_max = DFA.unpackEncodedStringToUnsignedChars(DFA7_maxS);
    static final short[] DFA7_accept = DFA.unpackEncodedString(DFA7_acceptS);
    static final short[] DFA7_special = DFA.unpackEncodedString(DFA7_specialS);
    static final short[][] DFA7_transition;

    static {
        int numStates = DFA7_transitionS.length;
        DFA7_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA7_transition[i] = DFA.unpackEncodedString(DFA7_transitionS[i]);
        }
    }

    class DFA7 extends DFA {

        public DFA7(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 7;
            this.eot = DFA7_eot;
            this.eof = DFA7_eof;
            this.min = DFA7_min;
            this.max = DFA7_max;
            this.accept = DFA7_accept;
            this.special = DFA7_special;
            this.transition = DFA7_transition;
        }
        public String getDescription() {
            return "39:1: stat returns [LSStatement s] : ( varlist1 '=' explist1 | functioncall | 'do' block 'end' | 'while' exp 'do' block 'end' | 'repeat' block 'until' exp | ifstat | 'for' NAME '=' e1= exp ',' e2= exp ( ',' e3= exp )? 'do' block 'end' | 'for' namelist 'in' explist1 'do' block 'end' | 'function' funcname funcbody | 'local' 'function' NAME funcbody | 'local' namelist ( '=' explist1 )? );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA7_1 = input.LA(1);

                         
                        int index7_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Lua()) ) {s = 10;}

                        else if ( (synpred6_Lua()) ) {s = 11;}

                         
                        input.seek(index7_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA7_2 = input.LA(1);

                         
                        int index7_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Lua()) ) {s = 10;}

                        else if ( (synpred6_Lua()) ) {s = 11;}

                         
                        input.seek(index7_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA7_7 = input.LA(1);

                         
                        int index7_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred12_Lua()) ) {s = 12;}

                        else if ( (synpred13_Lua()) ) {s = 13;}

                         
                        input.seek(index7_7);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA7_9 = input.LA(1);

                         
                        int index7_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Lua()) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index7_9);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 7, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA16_eotS =
        "\27\uffff";
    static final String DFA16_eofS =
        "\27\uffff";
    static final String DFA16_minS =
        "\1\4\24\0\2\uffff";
    static final String DFA16_maxS =
        "\1\102\24\0\2\uffff";
    static final String DFA16_acceptS =
        "\25\uffff\1\1\1\2";
    static final String DFA16_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\2\uffff}>";
    static final String[] DFA16_transitionS = {
            "\1\20\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\20\uffff\1"+
            "\17\11\uffff\1\1\1\2\1\3\1\16\1\21\3\uffff\1\22\2\uffff\1\4"+
            "\15\uffff\1\23\1\24",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA16_eot = DFA.unpackEncodedString(DFA16_eotS);
    static final short[] DFA16_eof = DFA.unpackEncodedString(DFA16_eofS);
    static final char[] DFA16_min = DFA.unpackEncodedStringToUnsignedChars(DFA16_minS);
    static final char[] DFA16_max = DFA.unpackEncodedStringToUnsignedChars(DFA16_maxS);
    static final short[] DFA16_accept = DFA.unpackEncodedString(DFA16_acceptS);
    static final short[] DFA16_special = DFA.unpackEncodedString(DFA16_specialS);
    static final short[][] DFA16_transition;

    static {
        int numStates = DFA16_transitionS.length;
        DFA16_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA16_transition[i] = DFA.unpackEncodedString(DFA16_transitionS[i]);
        }
    }

    class DFA16 extends DFA {

        public DFA16(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 16;
            this.eot = DFA16_eot;
            this.eof = DFA16_eof;
            this.min = DFA16_min;
            this.max = DFA16_max;
            this.accept = DFA16_accept;
            this.special = DFA16_special;
            this.transition = DFA16_transition;
        }
        public String getDescription() {
            return "()* loopback of 93:4: (e1= exp ',' )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA16_1 = input.LA(1);

                         
                        int index16_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA16_2 = input.LA(1);

                         
                        int index16_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA16_3 = input.LA(1);

                         
                        int index16_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA16_4 = input.LA(1);

                         
                        int index16_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA16_5 = input.LA(1);

                         
                        int index16_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA16_6 = input.LA(1);

                         
                        int index16_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA16_7 = input.LA(1);

                         
                        int index16_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA16_8 = input.LA(1);

                         
                        int index16_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA16_9 = input.LA(1);

                         
                        int index16_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA16_10 = input.LA(1);

                         
                        int index16_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA16_11 = input.LA(1);

                         
                        int index16_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA16_12 = input.LA(1);

                         
                        int index16_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA16_13 = input.LA(1);

                         
                        int index16_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA16_14 = input.LA(1);

                         
                        int index16_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA16_15 = input.LA(1);

                         
                        int index16_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA16_16 = input.LA(1);

                         
                        int index16_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA16_17 = input.LA(1);

                         
                        int index16_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_17);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA16_18 = input.LA(1);

                         
                        int index16_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_18);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA16_19 = input.LA(1);

                         
                        int index16_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_19);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA16_20 = input.LA(1);

                         
                        int index16_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Lua()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index16_20);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 16, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA17_eotS =
        "\14\uffff";
    static final String DFA17_eofS =
        "\14\uffff";
    static final String DFA17_minS =
        "\1\4\3\uffff\1\0\7\uffff";
    static final String DFA17_maxS =
        "\1\102\3\uffff\1\0\7\uffff";
    static final String DFA17_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\uffff\1\4\1\5\1\6\1\7\1\10\1\11\1\12";
    static final String DFA17_specialS =
        "\4\uffff\1\0\7\uffff}>";
    static final String[] DFA17_transitionS = {
            "\1\11\6\5\3\6\20\uffff\1\10\11\uffff\1\1\1\2\1\3\1\7\1\11\3"+
            "\uffff\1\12\2\uffff\1\4\15\uffff\2\13",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA17_eot = DFA.unpackEncodedString(DFA17_eotS);
    static final short[] DFA17_eof = DFA.unpackEncodedString(DFA17_eofS);
    static final char[] DFA17_min = DFA.unpackEncodedStringToUnsignedChars(DFA17_minS);
    static final char[] DFA17_max = DFA.unpackEncodedStringToUnsignedChars(DFA17_maxS);
    static final short[] DFA17_accept = DFA.unpackEncodedString(DFA17_acceptS);
    static final short[] DFA17_special = DFA.unpackEncodedString(DFA17_specialS);
    static final short[][] DFA17_transition;

    static {
        int numStates = DFA17_transitionS.length;
        DFA17_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA17_transition[i] = DFA.unpackEncodedString(DFA17_transitionS[i]);
        }
    }

    class DFA17 extends DFA {

        public DFA17(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 17;
            this.eot = DFA17_eot;
            this.eof = DFA17_eof;
            this.min = DFA17_min;
            this.max = DFA17_max;
            this.accept = DFA17_accept;
            this.special = DFA17_special;
            this.transition = DFA17_transition;
        }
        public String getDescription() {
            return "98:4: ( 'nil' | 'false' | 'true' | number | string | '...' | function | prefixexp | tableconstructor | unop e1= exp )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA17_4 = input.LA(1);

                         
                        int index17_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred29_Lua()) ) {s = 5;}

                        else if ( (true) ) {s = 11;}

                         
                        input.seek(index17_4);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 17, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA18_eotS =
        "\22\uffff";
    static final String DFA18_eofS =
        "\1\1\21\uffff";
    static final String DFA18_minS =
        "\1\4\1\uffff\17\0\1\uffff";
    static final String DFA18_maxS =
        "\1\100\1\uffff\17\0\1\uffff";
    static final String DFA18_acceptS =
        "\1\uffff\1\2\17\uffff\1\1";
    static final String DFA18_specialS =
        "\2\uffff\1\7\1\2\1\15\1\10\1\4\1\5\1\12\1\16\1\0\1\13\1\6\1\3\1"+
        "\14\1\11\1\1\1\uffff}>";
    static final String[] DFA18_transitionS = {
            "\1\1\17\uffff\1\1\1\uffff\7\1\1\uffff\10\1\6\uffff\2\1\1\uffff"+
            "\1\1\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13"+
            "\1\14\1\15\1\16\1\17\1\20",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            return "()* loopback of 108:5: ( binop e2= exp )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA18_10 = input.LA(1);

                         
                        int index18_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred35_Lua()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index18_10);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA18_16 = input.LA(1);

                         
                        int index18_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred35_Lua()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index18_16);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA18_3 = input.LA(1);

                         
                        int index18_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred35_Lua()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index18_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA18_13 = input.LA(1);

                         
                        int index18_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred35_Lua()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index18_13);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA18_6 = input.LA(1);

                         
                        int index18_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred35_Lua()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index18_6);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA18_7 = input.LA(1);

                         
                        int index18_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred35_Lua()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index18_7);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA18_12 = input.LA(1);

                         
                        int index18_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred35_Lua()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index18_12);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA18_2 = input.LA(1);

                         
                        int index18_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred35_Lua()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index18_2);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA18_5 = input.LA(1);

                         
                        int index18_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred35_Lua()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index18_5);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA18_15 = input.LA(1);

                         
                        int index18_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred35_Lua()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index18_15);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA18_8 = input.LA(1);

                         
                        int index18_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred35_Lua()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index18_8);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA18_11 = input.LA(1);

                         
                        int index18_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred35_Lua()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index18_11);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA18_14 = input.LA(1);

                         
                        int index18_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred35_Lua()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index18_14);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA18_4 = input.LA(1);

                         
                        int index18_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred35_Lua()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index18_4);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA18_9 = input.LA(1);

                         
                        int index18_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred35_Lua()) ) {s = 17;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index18_9);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 18, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA20_eotS =
        "\56\uffff";
    static final String DFA20_eofS =
        "\1\1\55\uffff";
    static final String DFA20_minS =
        "\1\4\2\uffff\6\0\45\uffff";
    static final String DFA20_maxS =
        "\1\100\2\uffff\6\0\45\uffff";
    static final String DFA20_acceptS =
        "\1\uffff\1\2\52\uffff\1\1\1\uffff";
    static final String DFA20_specialS =
        "\3\uffff\1\0\1\1\1\2\1\3\1\4\1\5\45\uffff}>";
    static final String[] DFA20_transitionS = {
            "\1\1\6\uffff\1\6\1\7\1\10\6\uffff\11\1\1\uffff\10\1\1\54\1"+
            "\3\4\uffff\1\4\1\1\1\54\1\1\1\5\20\1",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA20_eot = DFA.unpackEncodedString(DFA20_eotS);
    static final short[] DFA20_eof = DFA.unpackEncodedString(DFA20_eofS);
    static final char[] DFA20_min = DFA.unpackEncodedStringToUnsignedChars(DFA20_minS);
    static final char[] DFA20_max = DFA.unpackEncodedStringToUnsignedChars(DFA20_maxS);
    static final short[] DFA20_accept = DFA.unpackEncodedString(DFA20_acceptS);
    static final short[] DFA20_special = DFA.unpackEncodedString(DFA20_specialS);
    static final short[][] DFA20_transition;

    static {
        int numStates = DFA20_transitionS.length;
        DFA20_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA20_transition[i] = DFA.unpackEncodedString(DFA20_transitionS[i]);
        }
    }

    class DFA20 extends DFA {

        public DFA20(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 20;
            this.eot = DFA20_eot;
            this.eof = DFA20_eof;
            this.min = DFA20_min;
            this.max = DFA20_max;
            this.accept = DFA20_accept;
            this.special = DFA20_special;
            this.transition = DFA20_transition;
        }
        public String getDescription() {
            return "()* loopback of 115:7: ( varSuffix )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA20_3 = input.LA(1);

                         
                        int index20_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred37_Lua()) ) {s = 44;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index20_3);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA20_4 = input.LA(1);

                         
                        int index20_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred37_Lua()) ) {s = 44;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index20_4);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA20_5 = input.LA(1);

                         
                        int index20_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred37_Lua()) ) {s = 44;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index20_5);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA20_6 = input.LA(1);

                         
                        int index20_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred37_Lua()) ) {s = 44;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index20_6);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA20_7 = input.LA(1);

                         
                        int index20_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred37_Lua()) ) {s = 44;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index20_7);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA20_8 = input.LA(1);

                         
                        int index20_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred37_Lua()) ) {s = 44;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index20_8);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 20, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA23_eotS =
        "\53\uffff";
    static final String DFA23_eofS =
        "\1\1\52\uffff";
    static final String DFA23_minS =
        "\1\4\31\uffff\1\0\20\uffff";
    static final String DFA23_maxS =
        "\1\100\31\uffff\1\0\20\uffff";
    static final String DFA23_acceptS =
        "\1\uffff\1\2\44\uffff\1\1\4\uffff";
    static final String DFA23_specialS =
        "\32\uffff\1\0\20\uffff}>";
    static final String[] DFA23_transitionS = {
            "\1\1\6\uffff\3\46\6\uffff\1\1\1\uffff\7\1\1\uffff\10\1\1\uffff"+
            "\1\46\4\uffff\1\32\1\1\1\uffff\1\1\1\46\20\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA23_eot = DFA.unpackEncodedString(DFA23_eotS);
    static final short[] DFA23_eof = DFA.unpackEncodedString(DFA23_eofS);
    static final char[] DFA23_min = DFA.unpackEncodedStringToUnsignedChars(DFA23_minS);
    static final char[] DFA23_max = DFA.unpackEncodedStringToUnsignedChars(DFA23_maxS);
    static final short[] DFA23_accept = DFA.unpackEncodedString(DFA23_acceptS);
    static final short[] DFA23_special = DFA.unpackEncodedString(DFA23_specialS);
    static final short[][] DFA23_transition;

    static {
        int numStates = DFA23_transitionS.length;
        DFA23_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA23_transition[i] = DFA.unpackEncodedString(DFA23_transitionS[i]);
        }
    }

    class DFA23 extends DFA {

        public DFA23(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 23;
            this.eot = DFA23_eot;
            this.eof = DFA23_eof;
            this.min = DFA23_min;
            this.max = DFA23_max;
            this.accept = DFA23_accept;
            this.special = DFA23_special;
            this.transition = DFA23_transition;
        }
        public String getDescription() {
            return "()* loopback of 129:3: (n= nameAndArgs[$prefixexp::current] )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA23_26 = input.LA(1);

                         
                        int index23_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred40_Lua()) ) {s = 38;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index23_26);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 23, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA24_eotS =
        "\27\uffff";
    static final String DFA24_eofS =
        "\1\1\26\uffff";
    static final String DFA24_minS =
        "\1\4\11\uffff\1\0\14\uffff";
    static final String DFA24_maxS =
        "\1\60\11\uffff\1\0\14\uffff";
    static final String DFA24_acceptS =
        "\1\uffff\1\2\20\uffff\1\1\4\uffff";
    static final String DFA24_specialS =
        "\12\uffff\1\0\14\uffff}>";
    static final String[] DFA24_transitionS = {
            "\1\1\6\uffff\3\22\6\uffff\1\1\1\uffff\6\1\2\uffff\3\1\1\uffff"+
            "\4\1\1\uffff\1\22\4\uffff\1\12\3\uffff\1\22",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA24_eot = DFA.unpackEncodedString(DFA24_eotS);
    static final short[] DFA24_eof = DFA.unpackEncodedString(DFA24_eofS);
    static final char[] DFA24_min = DFA.unpackEncodedStringToUnsignedChars(DFA24_minS);
    static final char[] DFA24_max = DFA.unpackEncodedStringToUnsignedChars(DFA24_maxS);
    static final short[] DFA24_accept = DFA.unpackEncodedString(DFA24_acceptS);
    static final short[] DFA24_special = DFA.unpackEncodedString(DFA24_specialS);
    static final short[][] DFA24_transition;

    static {
        int numStates = DFA24_transitionS.length;
        DFA24_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA24_transition[i] = DFA.unpackEncodedString(DFA24_transitionS[i]);
        }
    }

    class DFA24 extends DFA {

        public DFA24(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 24;
            this.eot = DFA24_eot;
            this.eof = DFA24_eof;
            this.min = DFA24_min;
            this.max = DFA24_max;
            this.accept = DFA24_accept;
            this.special = DFA24_special;
            this.transition = DFA24_transition;
        }
        public String getDescription() {
            return "()+ loopback of 136:3: (n= nameAndArgs[$functioncall::current] )+";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA24_10 = input.LA(1);

                         
                        int index24_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred41_Lua()) ) {s = 18;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index24_10);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 24, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_funcblock_in_chunk58 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_block_in_funcblock88 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stat_in_block113 = new BitSet(new long[]{0x00001031CB500012L});
    public static final BitSet FOLLOW_20_in_block118 = new BitSet(new long[]{0x00001031CB500012L});
    public static final BitSet FOLLOW_laststat_in_block128 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_20_in_block133 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varlist1_in_stat159 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_21_in_stat161 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_explist1_in_stat163 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functioncall_in_stat183 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_22_in_stat213 = new BitSet(new long[]{0x00001031CB500010L});
    public static final BitSet FOLLOW_block_in_stat217 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_stat221 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_24_in_stat286 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_stat288 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_stat290 = new BitSet(new long[]{0x00001031CB500010L});
    public static final BitSet FOLLOW_block_in_stat294 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_stat298 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_25_in_stat344 = new BitSet(new long[]{0x00001031CB500010L});
    public static final BitSet FOLLOW_block_in_stat349 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_stat354 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_stat356 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ifstat_in_stat410 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_stat445 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_NAME_in_stat449 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_21_in_stat453 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_stat457 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_stat459 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_stat463 = new BitSet(new long[]{0x0000000010400000L});
    public static final BitSet FOLLOW_28_in_stat466 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_stat470 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_stat474 = new BitSet(new long[]{0x00001031CB500010L});
    public static final BitSet FOLLOW_block_in_stat478 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_stat482 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_stat528 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_namelist_in_stat533 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_stat537 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_explist1_in_stat539 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_stat541 = new BitSet(new long[]{0x00001031CB500010L});
    public static final BitSet FOLLOW_block_in_stat545 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_stat548 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_30_in_stat593 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_funcname_in_stat595 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_funcbody_in_stat597 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_stat610 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_stat612 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_NAME_in_stat614 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_funcbody_in_stat618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_stat666 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_namelist_in_stat668 = new BitSet(new long[]{0x0000000000200002L});
    public static final BitSet FOLLOW_21_in_stat671 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_explist1_in_stat673 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_ifstat699 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_ifstat703 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_33_in_ifstat705 = new BitSet(new long[]{0x00001031CB500010L});
    public static final BitSet FOLLOW_block_in_ifstat712 = new BitSet(new long[]{0x0000000C00800000L});
    public static final BitSet FOLLOW_34_in_ifstat721 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_ifstat725 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_33_in_ifstat727 = new BitSet(new long[]{0x00001031CB500010L});
    public static final BitSet FOLLOW_block_in_ifstat734 = new BitSet(new long[]{0x0000000C00800000L});
    public static final BitSet FOLLOW_35_in_ifstat745 = new BitSet(new long[]{0x00001031CB500010L});
    public static final BitSet FOLLOW_block_in_ifstat752 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_ifstat762 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_36_in_laststat782 = new BitSet(new long[]{0x00091F0040003FF2L,0x0000000000000006L});
    public static final BitSet FOLLOW_explist1_in_laststat787 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_37_in_laststat797 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_funcname831 = new BitSet(new long[]{0x000000C000000002L});
    public static final BitSet FOLLOW_38_in_funcname839 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_NAME_in_funcname843 = new BitSet(new long[]{0x000000C000000002L});
    public static final BitSet FOLLOW_39_in_funcname854 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_NAME_in_funcname858 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_in_varlist1887 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_28_in_varlist1894 = new BitSet(new long[]{0x0000100000000010L});
    public static final BitSet FOLLOW_var_in_varlist1898 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_NAME_in_namelist919 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_28_in_namelist927 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_NAME_in_namelist931 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_exp_in_explist1960 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_explist1962 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_explist1973 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_exp991 = new BitSet(new long[]{0xFFFC000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_41_in_exp1013 = new BitSet(new long[]{0xFFFC000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_42_in_exp1030 = new BitSet(new long[]{0xFFFC000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_number_in_exp1048 = new BitSet(new long[]{0xFFFC000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_string_in_exp1066 = new BitSet(new long[]{0xFFFC000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_43_in_exp1084 = new BitSet(new long[]{0xFFFC000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_function_in_exp1103 = new BitSet(new long[]{0xFFFC000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_prefixexp_in_exp1119 = new BitSet(new long[]{0xFFFC000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_tableconstructor_in_exp1134 = new BitSet(new long[]{0xFFFC000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_unop_in_exp1142 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_exp1146 = new BitSet(new long[]{0xFFFC000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_binop_in_exp1160 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_exp1164 = new BitSet(new long[]{0xFFFC000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_NAME_in_var1195 = new BitSet(new long[]{0x000150C000003802L});
    public static final BitSet FOLLOW_44_in_var1207 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_var1209 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_45_in_var1211 = new BitSet(new long[]{0x000150C000003800L});
    public static final BitSet FOLLOW_varSuffix_in_var1215 = new BitSet(new long[]{0x000150C000003802L});
    public static final BitSet FOLLOW_varSuffix_in_var1225 = new BitSet(new long[]{0x000150C000003802L});
    public static final BitSet FOLLOW_nameAndArgs_in_varSuffix1245 = new BitSet(new long[]{0x000150C000003800L});
    public static final BitSet FOLLOW_46_in_varSuffix1256 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_varSuffix1260 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_47_in_varSuffix1262 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_38_in_varSuffix1275 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_NAME_in_varSuffix1279 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varOrExp_in_prefixexp1311 = new BitSet(new long[]{0x0001108000003802L});
    public static final BitSet FOLLOW_nameAndArgs_in_prefixexp1321 = new BitSet(new long[]{0x0001108000003802L});
    public static final BitSet FOLLOW_varOrExp_in_functioncall1352 = new BitSet(new long[]{0x0001108000003800L});
    public static final BitSet FOLLOW_nameAndArgs_in_functioncall1362 = new BitSet(new long[]{0x0001108000003802L});
    public static final BitSet FOLLOW_var_in_varOrExp1386 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_varOrExp1401 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_varOrExp1403 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_45_in_varOrExp1405 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_39_in_nameAndArgs1431 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_NAME_in_nameAndArgs1435 = new BitSet(new long[]{0x0001108000003800L});
    public static final BitSet FOLLOW_args_in_nameAndArgs1452 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_args1476 = new BitSet(new long[]{0x00093F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_explist1_in_args1481 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_45_in_args1487 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tableconstructor_in_args1495 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_string_in_args1505 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_30_in_function1532 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_funcbody_in_function1536 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_funcbody1561 = new BitSet(new long[]{0x0000280000000010L});
    public static final BitSet FOLLOW_parlist1_in_funcbody1564 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_45_in_funcbody1570 = new BitSet(new long[]{0x00001031CB500010L});
    public static final BitSet FOLLOW_funcblock_in_funcbody1572 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_funcbody1575 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namelist_in_parlist11591 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_28_in_parlist11596 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_43_in_parlist11598 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_43_in_parlist11608 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_48_in_tableconstructor1631 = new BitSet(new long[]{0x000B5F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_fieldlist_in_tableconstructor1634 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_tableconstructor1639 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_fieldlist1654 = new BitSet(new long[]{0x0000000010100002L});
    public static final BitSet FOLLOW_fieldsep_in_fieldlist1659 = new BitSet(new long[]{0x00095F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_field_in_fieldlist1661 = new BitSet(new long[]{0x0000000010100002L});
    public static final BitSet FOLLOW_fieldsep_in_fieldlist1668 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_46_in_field1683 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_field1687 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_47_in_field1689 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_21_in_field1691 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_field1695 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_field1706 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_21_in_field1708 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_field1712 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_exp_in_field1728 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_fieldsep0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_50_in_binop1781 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_51_in_binop1790 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_52_in_binop1800 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_53_in_binop1810 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_54_in_binop1820 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_55_in_binop1830 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_56_in_binop1840 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_57_in_binop1849 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_58_in_binop1858 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_59_in_binop1867 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_60_in_binop1876 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_61_in_binop1885 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_62_in_binop1894 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_63_in_binop1903 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_64_in_binop1911 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_51_in_unop1930 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_65_in_unop1940 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_66_in_unop1949 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_51_in_number1967 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_number1971 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_51_in_number1978 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_FLOAT1_in_number1982 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_51_in_number1989 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_FLOAT2_in_number1993 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_51_in_number2000 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_FLOAT3_in_number2004 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_51_in_number2011 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_EXP_in_number2015 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HEX_in_number2021 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NORMALSTRING_in_string2037 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHARSTRING_in_string2044 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LONGSTRING_in_string2053 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varlist1_in_synpred5_Lua159 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_21_in_synpred5_Lua161 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_explist1_in_synpred5_Lua163 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functioncall_in_synpred6_Lua183 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_synpred12_Lua445 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_NAME_in_synpred12_Lua449 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_21_in_synpred12_Lua453 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_synpred12_Lua457 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_synpred12_Lua459 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_synpred12_Lua463 = new BitSet(new long[]{0x0000000010400000L});
    public static final BitSet FOLLOW_28_in_synpred12_Lua466 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_synpred12_Lua470 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_synpred12_Lua474 = new BitSet(new long[]{0x00001031CB500010L});
    public static final BitSet FOLLOW_block_in_synpred12_Lua478 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_synpred12_Lua482 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_synpred13_Lua528 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_namelist_in_synpred13_Lua533 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_synpred13_Lua537 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_explist1_in_synpred13_Lua539 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_synpred13_Lua541 = new BitSet(new long[]{0x00001031CB500010L});
    public static final BitSet FOLLOW_block_in_synpred13_Lua545 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_synpred13_Lua548 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_synpred15_Lua610 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_synpred15_Lua612 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_NAME_in_synpred15_Lua614 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_funcbody_in_synpred15_Lua618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_exp_in_synpred25_Lua960 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_synpred25_Lua962 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_number_in_synpred29_Lua1048 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_binop_in_synpred35_Lua1160 = new BitSet(new long[]{0x00091F0040003FF0L,0x0000000000000006L});
    public static final BitSet FOLLOW_exp_in_synpred35_Lua1164 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varSuffix_in_synpred37_Lua1225 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nameAndArgs_in_synpred40_Lua1321 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nameAndArgs_in_synpred41_Lua1362 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_var_in_synpred42_Lua1386 = new BitSet(new long[]{0x0000000000000002L});

}