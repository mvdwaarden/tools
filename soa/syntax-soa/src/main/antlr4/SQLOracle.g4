grammar SQLOracle;
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
	| enableComment 
	| comment 
	| lob
	| xmltype
;
createTable : 'create' 'table'  name=ID ('('
	attribute (',' (attribute | primarykeyConstraint)) *
	')')? addConstraint?
	dbmsspec? 
;	

attribute : 
 	ID type ('default' defaultValue)? ('not'? NULL)?
;

type : 
	name=ID ('(' ((INT|WILDCARD) (ID)?) (',' INT)?')')?
;

defaultValue : 
	'-'? INT|expression|NULL;

expression : 
	ID ('(' expression (',' expression)*')')? #FunctionExpression	
;

alterTable : 'alter' 'table' name=ID 
	addConstraint 
;

addConstraint : 'add' 'constraint' name=ID 
	(foreignKeyConstraint
	| checkConstraint	
	| uniqueConstraint
	) 'enable'?
;
uniqueConstraint : 'unique' colspec
;
foreignKeyConstraint: 'foreign' 'key' '(' from=ID ')' 
	'references' table=ID '(' to=ID ')' onDeleteCascade? 
;

checkConstraint : 
	'check' condition
;

condition : 
	condition 'or' condition #Disjunction
	| condition 'and' condition #Conjunction
	| ID? '(' condition ')' #BracketCondition
	| condition ('='|'<='|'>='|'>'|'<') condition #EqualsCondition
	| ID 'in' condition #InCondition
	| (~'(')* #BaseCondition
	
;

primarykeyConstraint : 
	'constraint' ID 'primary' 'key' colspec ('enable') 
;

enableComment : 'enablecomment' 'on' 'table' ID 'is' ID 
;

comment : 'comment' 'on' ('table'|'column') ID 'is' ID ';'?
;

createIndex : 'create' 'unique'? 'index' ID 'on' ID colspec dbmsspec?
;

colspec : '(' idFunction ('asc'|'desc')? (',' idFunction ('asc'|'desc')?)* ')'
;
	
idFunction : ID '(' idFunction ')' | ID; 
	
dbmsspec : ('logging')?
	('tablespace' ID)?
	('pctfree' INT)? 
	('initrans' INT)?
	('storage' '('
		('initial' INT) ?
		('next' INT)?
		('minextents' INT)?
		('maxextents' INT)?
		('buffer_pool' 'default')
	')')? 
;

lob:
	'lob' '(' ID ')' 'store' 'as' ID '('
		('enable' 'storage' 'in' 'row')?
		('chunk' INT)?
		'retention'? 
		('cache'|'nocache')?		
		'logging'?
	
	')'
;

xmltype: 'xmltype' ID 'store' 'as' 'binary' 'xml' 'allow' 'nonschema'
;

onDeleteCascade : 'on' 'delete' 'cascade'
;

WILDCARD : '*';
INT : DIGIT+ ;
NULL : 'null';
FLOAT : (DIGIT+)(','DIGIT+)? ;
CHAR : LCASE_ALPHA ;
ID : (ID_PART (DOT ID_PART?)*) | STRING;
ID_PART : (ALPHA)(ALPHA|DIGIT|SPECIAL)*;
fragment DIGIT : [0-9];
fragment SPECIAL : '$';
fragment ALPHA : LCASE_ALPHA|UCASE_ALPHA|'_';
fragment LCASE_ALPHA : [a-z];
fragment UCASE_ALPHA : [A-Z];
fragment DOT : '.';
fragment STRING : ('\'' (~'\'')* '\'')|('"' (~'"')* '"');
WS : ([ \t\n\r]+|('-'(~[\n\r])*)|('!!'(~[\n\r])*))  -> skip ;