package plp.enquanto.parser;

import java.util.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import org.antlr.v4.runtime.tree.TerminalNode;
import plp.enquanto.linguagem.Linguagem;
import plp.enquanto.linguagem.Linguagem.*;

public class MeuListener extends EnquantoBaseListener {
    private final Leia leia = new Leia();
    private final Skip skip = new Skip();
    private Ambiente ambienteAtual = Linguagem.AMBIENTE;

    private final ParseTreeProperty<Object> values = new ParseTreeProperty<Object>();

    private Programa programa;
    private Map<String, Funcao> funcoes = new Hashtable<String, Funcao>();

    public Programa getPrograma() {
        return programa;
    }

    private void setValue(final ParseTree node, final Object value) {
        values.put(node, value);
    }

    private Object getValue(final ParseTree node) {
        return values.get(node);
    }

    @Override
    public void exitBooleano(final EnquantoParser.BooleanoContext ctx) {
        setValue(ctx, new Booleano(ctx.getText().equals("verdadeiro")));
    }

    @Override
    public void exitLeia(final EnquantoParser.LeiaContext ctx) {
        setValue(ctx, leia);
    }

    @Override
    public void exitSe(final EnquantoParser.SeContext ctx) {
        final Bool condicao = (Bool) getValue(ctx.bool(0));

        final Comando entao = (Comando) getValue(ctx.comando(0));
        final Comando senao = (Comando) getValue(ctx.comando(ctx.bool().size()));

        final List<SenaoSe> listaSenaoSe = new ArrayList<SenaoSe>();

        if (ctx.bool().size() > 1) {
            for (int i = 1; i < ctx.bool().size(); i++) {
                final Bool _condicao = (Bool) getValue(ctx.bool(i));
                final Comando _entao = (Comando) getValue(ctx.comando(i));

                listaSenaoSe.add(new SenaoSe(_condicao, _entao));
            }
        }

        setValue(ctx, new Se(condicao, entao, listaSenaoSe, senao));
    }

    @Override
    public void exitPara(EnquantoParser.ParaContext ctx) {
        final Id id = new Id(ctx.ID().getText(), ambienteAtual);
        final Expressao de = (Expressao) getValue(ctx.expressao(0));
        final Expressao ate = (Expressao) getValue(ctx.expressao(1));
        final Expressao passo = (Expressao) getValue(ctx.INT());

        final Comando faca = (Comando) getValue(ctx.comando());

        setValue(ctx, new Para(id, de, ate, passo, faca));
    }

    @Override
    public void exitInteiro(final EnquantoParser.InteiroContext ctx) {
        setValue(ctx, new Inteiro(Integer.parseInt(ctx.getText())));
    }

    @Override
    public void exitSkip(final EnquantoParser.SkipContext ctx) {
        setValue(ctx, skip);
    }

    @Override
    public void exitEscreva(final EnquantoParser.EscrevaContext ctx) {
        final Expressao exp = (Expressao) getValue(ctx.expressao());
        setValue(ctx, new Escreva(exp));
    }

    @Override
    public void exitPrograma(final EnquantoParser.ProgramaContext ctx) {
        @SuppressWarnings("unchecked")
        final List<Instrucao> cmds = (List<Instrucao>) getValue(ctx.seqInstr());
        programa = new Programa(cmds);
        setValue(ctx, programa);
    }

    @Override
    public void exitId(final EnquantoParser.IdContext ctx) {
        setValue(ctx, new Id(ctx.ID().getText(), ambienteAtual));
    }

    @Override
    public void exitSeqComando(final EnquantoParser.SeqComandoContext ctx) {
        final List<Comando> comandos = new ArrayList<Comando>();
        for (EnquantoParser.ComandoContext c : ctx.comando()) {
            comandos.add((Comando) getValue(c));
        }
        setValue(ctx, comandos);
    }

    @Override
    public void enterDeclFuncao(EnquantoParser.DeclFuncaoContext ctx) {
        ambienteAtual = new Ambiente(ambienteAtual);

        final String id = ctx.ID().getText();
        final Funcao funcao = new Funcao(id, ambienteAtual);

        funcoes.put(id, funcao);
    }

    @Override
    public void exitDeclFuncao(EnquantoParser.DeclFuncaoContext ctx) {
        final String id = ctx.ID().getText();
        final Expressao retorno = (Expressao) getValue(ctx.expressao());
        final Funcao funcao = funcoes.get(id);

        final List<String> args = new ArrayList<String>();
        final EnquantoParser.ArgListContext argsCtx = ctx.argList();

        for (TerminalNode idNode : argsCtx.ID()) {
            args.add(idNode.getText());
        }

        funcao.setArgs(args);
        funcao.setRetorno(retorno);
        ambienteAtual = ambienteAtual.getLegado();

        setValue(ctx, funcao);
    }

    @Override
    public void exitExprExecFuncao(EnquantoParser.ExprExecFuncaoContext ctx) {
        final String id = ctx.execFuncao().ID().getText();
        final Funcao funcao = funcoes.get(id);
        final EnquantoParser.ParamListContext paramCtx = ctx.execFuncao().paramList();
        final List<Expressao> params = new ArrayList<Expressao>();

        for (EnquantoParser.ExpressaoContext expCtx : paramCtx.expressao()) {
            params.add((Expressao) getValue(expCtx));
        }

        setValue(ctx, new ChamadaFuncao(funcao, params));
    }

    @Override
    public void exitDecl(EnquantoParser.DeclContext ctx) {
        setValue(ctx, getValue(ctx.declFuncao()));
    }

    @Override
    public void exitInstrucao(EnquantoParser.InstrucaoContext ctx) {
        final Instrucao instr = (Instrucao) getValue(ctx.getChild(0));

        setValue(ctx, instr);
    }

    @Override
    public void exitSeqInstr(EnquantoParser.SeqInstrContext ctx) {
        List<Instrucao> comandos = new ArrayList<Instrucao>();

        for (EnquantoParser.InstrContext instrCtx : ctx.instr()) {
            comandos.add((Instrucao) getValue(instrCtx));
        }

        setValue(ctx, comandos);
    }

    @Override
    public void exitAtribuicao(final EnquantoParser.AtribuicaoContext ctx) {
        final String id = ctx.ID().getText();
        final Expressao exp = (Expressao) getValue(ctx.expressao());

        setValue(ctx, new Atribuicao(id, exp, ambienteAtual));
    }

    @Override
    public void exitBloco(final EnquantoParser.BlocoContext ctx) {
        @SuppressWarnings("unchecked")
        final List<Comando> cmds = (List<Comando>) getValue(ctx.seqComando());
        setValue(ctx, new Bloco(cmds));
    }

    @Override
    public void exitExprArit(EnquantoParser.ExprAritContext ctx) {
        setValue(ctx, getValue(ctx.getChild(0)));
    }

    @Override
    public void exitExprAdd(final EnquantoParser.ExprAddContext ctx) {
        exitExprBin(ctx);
    }

    @Override
    public void exitExprMul(EnquantoParser.ExprMulContext ctx) {
        exitExprBin(ctx);
    }

    @Override
    public void exitExprPot(EnquantoParser.ExprPotContext ctx) {
        exitExprBin(ctx);
    }

    private void exitExprBin(ParserRuleContext ctx) {
        if (ctx.children.size() == 1) {
            setValue(ctx, getValue(ctx.getChild(0)));
            return;
        }

        Expressao esq = (Expressao) getValue(ctx.getChild(0));

        for (int i = 2; i < ctx.getChildCount(); i += 2) {
            final String op = ctx.getChild(i - 1).getText();
            final ParseTree expCtx = ctx.getChild(i);
            final Expressao dir = (Expressao) getValue(expCtx);

            esq = exprBin(op, esq, dir);
        }

        setValue(ctx, esq);
    }

    private Expressao exprBin(String op, Expressao esq, Expressao dir) {
        final Expressao exp;

        if ("+".equals(op))
            exp = new ExpSoma(esq, dir);
        else if("*".equals(op))
            exp = new ExpMul(esq, dir);
        else if ("-".equals(op))
            exp = new ExpSub(esq, dir);
        else if ("*".equals(op))
            exp = new ExpMul(esq, dir);
        else if ("/".equals(op))
            exp = new ExpDiv(esq, dir);
        else if ("^".equals(op))
            exp = new ExpPot(esq, dir);
        else
            exp = new ExpSoma(esq, dir);

        return exp;
    }

    @Override
    public void exitEnquanto(final EnquantoParser.EnquantoContext ctx) {
        final Bool condicao = (Bool) getValue(ctx.bool());
        final Comando comando = (Comando) getValue(ctx.comando());
        setValue(ctx, new Enquanto(condicao, comando));
    }

    @Override
    public void exitEscolha(EnquantoParser.EscolhaContext ctx) {
        final int qtCasos = ctx.comando().size() - 1;

        final Expressao padrao = (Expressao) getValue(ctx.expressao());
        final Comando outro = (Comando) getValue(ctx.comando(qtCasos));

        final Map<Expressao, Comando> comandos = new LinkedHashMap<Expressao, Comando>();

        for (int i = 0; i < qtCasos; i++) {
            final Expressao _padrao = new Inteiro(Integer.valueOf(ctx.INT(i).getText()));
            final Comando _comando = (Comando) getValue(ctx.comando(i));

            comandos.put(_padrao, _comando);
        }

        setValue(ctx, new Escolha(padrao, comandos, outro));
    }

    @Override
    public void exitBoolPar(final EnquantoParser.BoolParContext ctx) {
        setValue(ctx, getValue(ctx.bool()));
    }

    @Override
    public void exitNaoLogico(final EnquantoParser.NaoLogicoContext ctx) {
        final Bool b = (Bool) getValue(ctx.bool());
        setValue(ctx, new NaoLogico(b));
    }

    @Override
    public void exitExprPar(final EnquantoParser.ExprParContext ctx) {
        setValue(ctx, getValue(ctx.expressao()));
    }

    @Override
    public void exitExprNeg(EnquantoParser.ExprNegContext ctx) {
        final Expressao exp = (Expressao) getValue(ctx.expressao());

        setValue(ctx, new ExpNeg(exp));
    }

    @Override
    public void exitExiba(final EnquantoParser.ExibaContext ctx) {
        final String t = ctx.Texto().getText();
        final String texto = t.substring(1, t.length() - 1);
        setValue(ctx, new Exiba(texto));
    }

    @Override
    public void exitExprBool(final EnquantoParser.ExprBoolContext ctx) {
        final Object esq = getValue(ctx.getChild(0));
        final Object dir = getValue(ctx.getChild(2));
        final String op = ctx.getChild(1).getText();
        final Bool exp;

        if ("e".equals(op))
            exp = new ELogico((Bool) esq, (Bool) dir);
        else if ("ou".equals(op))
            exp = new OuLogico((Bool) esq, (Bool) dir);
        else if ("xor".equals(op))
            exp = new XorLogico((Bool) esq, (Bool) dir);
        else if ("=".equals(op))
            exp = new ExpIgual((Expressao) esq, (Expressao) dir);
        else if ("<".equals(op))
            exp = new ExpMenor((Expressao) esq, (Expressao) dir);
        else if (">".equals(op))
            exp = new ExpMaior((Expressao) esq, (Expressao) dir);
        else if ("<=".equals(op))
            exp = new ExpMenorIgual((Expressao) esq, (Expressao) dir);
        else if (">=".equals(op))
            exp = new ExpMaiorIgual((Expressao) esq, (Expressao) dir);
        else if ("<>".equals(op))
            exp = new ExpDesigual((Expressao) esq, (Expressao) dir);
        else
            exp = new ExpIgual((Expressao) esq, (Expressao) dir);

        setValue(ctx, exp);
    }
}
