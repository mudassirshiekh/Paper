package io.papermc.generator.rewriter.types.simple;

import io.papermc.generator.rewriter.types.registry.RegistryFieldRewriter;
import io.papermc.generator.utils.Formatting;
import io.papermc.typewriter.parser.Lexer;
import io.papermc.typewriter.parser.token.CharSequenceBlockToken;
import io.papermc.typewriter.parser.token.CharSequenceToken;
import io.papermc.typewriter.parser.token.TokenType;
import io.papermc.typewriter.parser.SequenceTokens;
import io.papermc.typewriter.replace.SearchMetadata;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class VillagerProfessionRewriter extends RegistryFieldRewriter<VillagerProfession> {

    public VillagerProfessionRewriter() {
        super(Registries.VILLAGER_PROFESSION, "getProfession");
    }

    private static final Set<TokenType> FORMAT_TOKENS = EnumSet.of(
        TokenType.COMMENT,
        TokenType.SINGLE_COMMENT,
        TokenType.MARKDOWN_JAVADOC // for now ignore
    );

    private @MonotonicNonNull Map<String, List<String>> javadocsPerConstant;

    private static class ConstantInfo {
        private @MonotonicNonNull String constantName;
        private @MonotonicNonNull List<String> javadocs;

        public void constantName(String name) {
            this.constantName = name;
        }

        public void javadocs(List<String> javadocs) {
            this.javadocs = javadocs;
        }

        public String constantName() {
            return this.constantName;
        }

        public List<String> javadocs() {
            return this.javadocs;
        }

        public boolean isEmpty() {
            return this.constantName == null || this.javadocs == null;
        }
    }

    private Map<String, List<String>> parseConstantJavadocs(String content) {
        Map<String, List<String>> map = new HashMap<>();

        Lexer lex = new Lexer(content.toCharArray());
        SequenceTokens.wrap(lex, FORMAT_TOKENS)
            .group(action -> {
                ConstantInfo info = new ConstantInfo();
                action
                    .map(TokenType.JAVADOC, token -> {
                        info.javadocs(((CharSequenceBlockToken) token).value());
                    }, SequenceTokens.TokenTask::asOptional)
                    .skipQualifiedName(Set.of(TokenType.JAVADOC))
                    .map(TokenType.IDENTIFIER, token -> {
                        info.constantName(((CharSequenceToken) token).value());
                    })
                    .skip(TokenType.IDENTIFIER)
                    .skipClosure(TokenType.LPAREN, TokenType.RPAREN, true)
                    .map(TokenType.SECO, $ -> {
                        if (!info.isEmpty()) {
                            map.put(info.constantName(), info.javadocs());
                        }
                    });
            }, SequenceTokens.TokenTask::asRepeatable)
            .execute();
        /*
        for enums:
        SequenceTokens.wrap(lex, FORMAT_TOKENS)
            .group(action -> {
                ConstantInfo info = new ConstantInfo();
                action
                    .map(TokenType.JAVADOC, token -> {
                        info.javadocs(((CharSequenceBlockToken) token).value());
                    }, SequenceTokens.TokenTask::asOptional)
                    .map(TokenType.IDENTIFIER, token -> {
                        info.constantName(((CharSequenceToken) token).value());
                    })
                    .skipClosure(TokenType.LPAREN, TokenType.RPAREN, true)
                    .skipClosure(TokenType.LSCOPE, TokenType.RSCOPE, true)
                    .mapMulti(Set.of(TokenType.CO, TokenType.SECO), $ -> {
                        // this part will probably fail for the last entry for enum without end (,;)
                        if (!info.isEmpty()) {
                            map.put(info.constantName(), info.javadocs());
                        }
                    });
            }, SequenceTokens.TokenTask::asRepeatable)
            .execute();
        */

        return map;
    }

    @Override
    protected void insert(SearchMetadata metadata, StringBuilder builder) {
        this.javadocsPerConstant = parseConstantJavadocs(metadata.replacedContent());
        super.insert(metadata, builder);
    }

    @Override
    protected void rewriteJavadocs(Holder.Reference<VillagerProfession> reference, String indent, StringBuilder builder) {
        String constantName = Formatting.formatKeyAsField(reference.key().location().getPath());
        if (this.javadocsPerConstant.containsKey(constantName)) {
            builder.append(indent).append("/**");
            builder.append('\n');
            for (String line : this.javadocsPerConstant.get(constantName)) {
                builder.append(indent).append(" * ").append(line);
                builder.append('\n');
            }
            builder.append(indent).append(" */");
            builder.append('\n');
        }
    }
}
