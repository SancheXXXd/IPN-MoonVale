lexer grammar ProfilesLexer;


WS : [\p{white_space}]+ -> skip ;



PROFILE      : 'profile';
ACTIVATE     : 'activate';

HOT1         : 'HOT1';
HOT2         : 'HOT2';
HOT3         : 'HOT3';
HOT4         : 'HOT4';
HOT5         : 'HOT5';
HOT6         : 'HOT6';
HOT7         : 'HOT7';
HOT8         : 'HOT8';
HOT9         : 'HOT9';
CHESTPLATE   : 'CHEST';
LEGS         : 'LEGS';
FEET         : 'FEET';
HEAD         : 'HEAD';
OFFHAND      : 'OFFHAND';

ORDER        : 'order' -> pushMode(customName); // "@order" is also customName

//ENCHANTMENTS: '"Enchantments"';

COMMA : ',' ;
//fragment LBRACK : '[';
//LBrack : WS? LBRACK;
LPAREN : '(' -> pushMode(customName); // item name or component data
RPAREN : ')' ;
LBRACK : '[' ;
RBRACK : ']' ;
LBRACE : '{' ;
RBRACE : '}' ;
DQUOTE : '"' ;
LID    : 'id';
//LVL    : 'lvl';
//S      : 's';

HASH : '#';
COLON : ':';
//SEMICOLON : ';';

//fragment ENCHANTMENTS    : 'Enchantments';
//COMPONENT      : '"Component" :';

ARROW : '->';

//Arrow: WS? ARROW ;

fragment ID: [a-zA-Z0-9_\-]+;


fragment NUMBER: [0-9]+([a-z])?;

Level: NUMBER;

Id :  ID;

// somehow empty identifier is allowed and resolved to `minecraft:`, so do it
// also supports wildcard
fragment NAMESPACE: [0-9a-z_\-.];
fragment PATH: [0-9a-z_\-./];
NamespacedId: DQUOTE (NAMESPACE* COLON)? PATH* DQUOTE;
NamespacedTag: DQUOTE HASH (NAMESPACE* COLON)? PATH* DQUOTE;
IdWildcard: DQUOTE ((NAMESPACE | '*')* COLON)? (PATH | '*')* DQUOTE;

mode customName;
WS_customName: WS -> skip;

STRING
   : DQUOTE (ESC | SAFECODEPOINT)* DQUOTE -> popMode; // end customName context

fragment ESC
   : '\\' (["\\/bfnrt] | UNICODE)
   ;
fragment UNICODE
   : 'u' HEX HEX HEX HEX
   ;
fragment HEX
   : [0-9a-fA-F]
   ;
fragment SAFECODEPOINT
   : ~ ["\\\u0000-\u001F]
   ;


