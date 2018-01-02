grammar SQLMicrosoft;
@lexer::header {
  package nl.ordina.tools.soa.parsers;
}

@parser::header {
package nl.ordina.tools.soa.parsers;
import nl.ordina.tools.ruleparser.ParseUtil;
}

file : ddl+;

ddl : 
	createTable 
	| createIndex 
	| alterTable 
	| go
	| set
	| use
;
createTable : 'create' 'table'  name=ID ('('
	attribute (',' (attribute | primarykeyConstraint)) *
	')')? addConstraint?
	dbmsspec? 
;	

attribute : 
 	ID type identity? ('default' defaultValue)? ('(' 'max' ')')? ('not'? 'for' 'replication')? ('not'? 'null')?
;

identity : 'identity' '(' INT ',' INT ')'
;


type : 
	ID ('(' ((INT|WILDCARD) (ID)?) (',' INT)?')')?
;

defaultValue : 
	'-'? INT|expression;

expression : 
	ID ('(' expression (',' expression)*')')? #FunctionExpression	
;

primarykeyConstraint : 
	'constraint' name=ID 'primary' 'key' 'clustered'? colspec with dbmsspec
;

alterTable : 'alter' 'table' name=ID with?
	( addConstraint 
	| checkConstraint
	)	
;

addConstraint : 'add' 'constraint' name=ID 
	(foreignKeyConstraint
	| uniqueConstraint
	) 
;
uniqueConstraint : 'unique' colspec
;

foreignKeyConstraint: 'foreign' 'key' '(' from=ID ')' 
	'references' table=ID '(' to=ID ')' onDeleteCascade? ('not'? 'for' 'replication')? 
;

checkConstraint : 
	'check' 'constraint' condition
;

condition : 
	condition 'or' condition #Disjunction
	| condition 'and' condition #Conjunction
	| ID? '(' condition ')' #BracketCondition
	| condition ('='|'<='|'>='|'>'|'<') condition #EqualsCondition
	| ID 'in' condition #InCondition
	| (~'(')* #BaseCondition
	
;

createIndex : 'create' 'unique'? 'index' ID 'on' ID colspec dbmsspec?
;

colspec : '(' ID ('asc'|'desc')? (',' ID ('asc'|'desc')?)* ')'
;

with : 'with' (('(' 
				ID '=' ('off'|'on') (',' ID '=' ('off'|'on'))*				
		')')|ID) 		
;

dbmsspec : 'on' ID
;


set : 'set' ID ('on'|'off')
;

onDeleteCascade : 'on' 'delete' 'cascade'
;

go : 'go'
;

use : 'use' ID
;


ID : (ID_PART (DOT ID_PART?)*) | STRING;
ID_PART : '['? (ALPHA)(ALPHA|DIGIT|SPECIAL)* ']'?
;
WILDCARD : '*';
INT : DIGIT+ ;
CHAR : LCASE_ALPHA ;
fragment DIGIT : [0-9];
fragment SPECIAL : '$';
fragment ALPHA : LCASE_ALPHA|UCASE_ALPHA|'_';
fragment LCASE_ALPHA : [a-z];
fragment UCASE_ALPHA : [A-Z];
fragment DOT : '.';
fragment STRING : ('\'' (~'\'')* '\'')|('"' (~'"')* '"');
WS : ([ \t\n\r]+|('-'(~[\n\r])*)|('!!'(~[\n\r])*)|('/*' .*? '*/')) -> skip ;