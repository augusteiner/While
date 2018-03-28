package plp.enquanto.parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import plp.enquanto.linguagem.Linguagem.*;

public class MeuListener extends EnquantoBaseListener {
    private final Leia leia = new Leia();
    private final Skip skip = new Skip();
    private final ParseTreeProperty<Object> values = new ParseTreeProperty<Object>();

    private Programa programa;

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
        final Bool condicao = (Bool) getValue(ctx.bool());
        final Comando entao = (Comando) getValue(ctx.comando(0));
        final Comando senao = (Comando) getValue(ctx.comando(1));
        setValue(ctx, new Se(condicao, entao, senao));
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
        final List<Comando> cmds = (List<Comando>) getValue(ctx.seqComando());
        programa = new Programa(cmds);
        setValue(ctx, programa);
    }

    @Override
    public void exitId(final EnquantoParser.IdContext ctx) {
        setValue(ctx, new Id(ctx.ID().getText()));
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
    public void exitAtribuicao(final EnquantoParser.AtribuicaoContext ctx) {
        final String id = ctx.ID().getText();
        final Expressao exp = (Expressao) getValue(ctx.expressao());
        setValue(ctx, new Atribuicao(id, exp));
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
    public void exitBoolPar(final EnquantoParser.BoolParContext ctx) {
        setValue(ctx, getValue(ctx.bool()));
    }

    @Override
    public void exitNaoLogico(final EnquantoParser.NaoLogicoContext ctx) {
        final Bool b = (Bool) getValue(ctx.bool());
        setValue(ctx, new NaoLogico(b));
    }

    @Override
    public void exitExpPar(final EnquantoParser.ExpParContext ctx) {
        setValue(ctx, getValue(ctx.expressao()));
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
