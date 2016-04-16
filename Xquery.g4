/**
 * Define a grammar called Xquery
 */
grammar Xquery;

// XPath, entry point
xp  : ap
	| rp
	| f
	;    

// Xquery sub-language for the project, entry point
xq	: Var
	| StringConstant
	| ap
	| '(' xq ')'
	| xq ',' xq
	| xq '/' rp
	| xq '//' rp
	| '<' TagName '>' '{' xq '}' '</' TagName '>'
	| forClause (letClause | /*epsilon*/) (whereClause | /*epsilon*/) returnClause
	| letClause xq
	;
		
// absolute path 
ap 	: 'doc("' FileName '")/' rp 
	| 'doc("' FileName '")//' rp
	| 'document("' FileName '")/' rp 
	| 'document("' FileName '")//' rp 
	;
	
// relative path
rp	: TagName
	| '*'
	| '..'
	| '.'
	| 'text()'
	| '@' AttName
	| '(' rp ')'
	| rp '/' rp
	| rp '//' rp
	| rp '[' f ']'
	| rp ',' rp 
	;

//path filter
f	: rp
	| rp '=' rp
	| rp 'eq' rp
	| rp '==' rp
	| rp 'is' rp
	| '(' f ')'
	| f 'and' f
	| f 'or' f
	| 'not' f
	;	

forClause	: 'for' Var 'in' xq (',' Var 'in' xq)*;

letClause	: 'let' Var ':=' xq (',' Var ':=' xq)*;

whereClause	: 'where' cond;

returnClause: 'return' xq;

cond	: xq '=' xq
		| xq 'eq' xq
		| xq '==' xq
		| xq 'is' xq
		| 'empty(' xq ')'
		| 'some' Var 'in' xq (',' Var 'in' xq)* 'satisfies' cond
		| '(' cond ')'
		| cond 'and' cond
		| cond 'or' cond
		| 'not' cond
		;

// Literals must be capitalized
FileName	: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '.' | '_' | '/' )( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '.' | '_' | '/' )*;

TagName 	: ( 'a' .. 'z' | 'A' .. 'Z' | '_' )( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*;

AttName 	: ( 'a' .. 'z' | 'A' .. 'Z' | '_' )( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*;

Var			: '$' ( 'a' .. 'z' | 'A' .. 'Z' | '_' )( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*;

StringConstant	: '"' ~('\n'|'\r')* '"';

Comment 	:  '//' ~( '\r' | '\n' )* -> skip;
  
WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

