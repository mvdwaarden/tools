grammar Statemachine;

@lexer::header {
  package nl.ordina.tools.parse;
}
 
@parser::header {
package nl.ordina.tools.parse;
}

statemachine : 
	transitionTable
;

transitionTable : 'transitions' ID guardedTransition+;
 
guardedTransition : condition ':' transitions;
transitions : transition*;
transition : state '->' state;
condition : condition condition 'AND' #And
			| condition condition 'OR' #Or			
			| condition 'NOT' #Not
			| ID #Input
			| BOOL_CONST #Const
;
state : ID;
BOOL_CONST : 'true' | 'false';
ID : (ALPHA)(ALPHA|DIGIT)*;
INT : DIGIT+ ;
FLOAT : (DIGIT+)(','DIGIT+)? ;
CHAR : LCASE_ALPHA ;
fragment DIGIT : [0-9];
fragment ALPHA : LCASE_ALPHA|UCASE_ALPHA|'_';
fragment LCASE_ALPHA : [a-z];
fragment UCASE_ALPHA : [A-Z];
WS : ([ \t\n\r]+|('--'(~[\n\r])*)|('!!'(~[\n\r])*))  -> skip ;
