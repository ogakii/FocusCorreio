# FocusCorreio

Plugin de correio para servidores Bukkit, Spigot e Paper. Ele cria um `/correio` com menu de recompensas pendentes, ideal para premios de eventos, crates, chaves, bosses, drops especiais e comandos da equipe.

O nome do plugin e `FocusCorreio`, mas dentro do jogo o menu aparece apenas como **Correio**.

## Destaques

- Menu `/correio` com itens centralizados.
- Botao para resgatar tudo.
- Paginas automaticas quando houver muitas recompensas.
- Resgate seguro: se o inventario estiver cheio, o item continua no correio.
- Entrega por comando para jogadores online ou offline.
- Entrega do item que o staff esta segurando.
- Aviso automatico ao entrar no servidor.
- Recompensa opcional para quem matar o Ender Dragon.
- Configuracao simples por `config.yml`.

## Compatibilidade

- Java 17 ou superior.
- Paper, Spigot ou Bukkit com API 1.20+.
- Testado em ambiente Paper moderno.

## Instalacao Rapida

1. Baixe `FocusCorreio-2.0.0.jar`.
2. Coloque o jar na pasta `plugins` do servidor.
3. Reinicie o servidor.
4. Entre no jogo e use:

```text
/correio
```

O plugin vai criar a pasta:

```text
plugins/FocusCorreio/
```

## Comandos

Jogadores comuns devem receber somente `focuscorreio.usar`. Essa permissao deixa abrir o menu, trocar de pagina pelo menu e resgatar itens.

Nao entregue `focuscorreio.admin` nem `focuscorreio.*` para jogadores comuns. Essa permissao e apenas para staff, console e plugins de evento, porque permite enviar recompensas para outros jogadores.

| Comando | Permissao | O que faz |
| --- | --- | --- |
| `/correio` | `focuscorreio.usar` | Abre o menu do correio. |
| `/correio resgatar` | `focuscorreio.usar` | Resgata tudo que couber no inventario. |
| `/correio ajuda` | `focuscorreio.usar` | Mostra os comandos disponiveis. |
| `/correio adicionar <jogador> <material> <quantia> <titulo...>` | `focuscorreio.admin` | Envia uma recompensa por material. |
| `/correio enviar <jogador> <titulo...>` | `focuscorreio.admin` | Envia o item que esta na mao do staff. |
| `/correio listar <jogador>` | `focuscorreio.admin` | Lista as recompensas pendentes. |
| `/correio limpar <jogador>` | `focuscorreio.admin` | Limpa o correio de um jogador. |
| `/correio reload` | `focuscorreio.admin` | Recarrega `config.yml` e `data.yml`. |

## Exemplos

Enviar diamantes:

```text
/correio adicionar Steve DIAMOND 3 Premio do Evento
```

Enviar uma chave:

```text
/correio adicionar Steve TRIPWIRE_HOOK 1 Chave Lendaria
```

Enviar o item da sua mao:

```text
/correio enviar Steve Premio Especial
```

## Menu

O menu tem 54 slots. As recompensas aparecem centralizadas nas 5 primeiras linhas, e a ultima linha fica reservada para controles.

```text
[ recompensas centralizadas                         ]
[ recompensas centralizadas                         ]
[ recompensas centralizadas                         ]
[ recompensas centralizadas                         ]
[ recompensas centralizadas                         ]
[ voltar ] [ resgatar tudo ] [ atualizar ] [ proxima ]
```

Voce pode editar nomes, lore e cores em `plugins/FocusCorreio/config.yml`.

## Recompensa do Dragao

O FocusCorreio tem uma recompensa automatica opcional para quem matar o Ender Dragon.

Ela vem desativada por padrao para evitar recompensa duplicada quando voce usa outro plugin de dragao.

Para configurar pelo jogo:

```text
/correio dragao definir DRAGON_EGG 1 Recompensa do Dragao
/correio dragao ativar
```

Para desativar:

```text
/correio dragao desativar
```

## Integracao com EnderDragon Spawner Plugin

Se voce usa a versao integrada do `EnderDragonSpawnerPlugin`, deixe a recompensa propria do FocusCorreio desativada:

```yaml
automatic-rewards:
  ender-dragon:
    enabled: false
```

No plugin do dragao, use `delivery-mode: CORREIO` ou `delivery-mode: AUTO`.

## Usar com TitansBattle

O `FocusCorreio` tambem pode receber recompensas do `TitansBattle`.

Funciona assim: o TitansBattle termina o evento, descobre o vencedor e executa um comando pelo console. Esse comando chama o FocusCorreio e coloca o item no correio do jogador.

No arquivo do evento do TitansBattle, dentro de `prizes`, coloque a recompensa como comando. Esse comando deve ser executado pelo console do evento, nao por jogador comum:

```text
correio adicionar %player% MATERIAL QUANTIA Nome da Recompensa
```

Exemplo para o primeiro lugar:

```yaml
prizes:
  FIRST:
    member.commands.enabled: true
    member.commands.command_list:
      - "correio adicionar %player% DIAMOND 16 Premio do Gladiador"
      - "correio adicionar %player% TRIPWIRE_HOOK 1 Chave do Evento"
```

Depois de editar o evento, recarregue o TitansBattle:

```text
/tb reload
```

Quando o evento acabar, o vencedor abre:

```text
/correio
```

O jogador vencedor nao precisa de permissao de envio. Ele precisa apenas de `focuscorreio.usar` para abrir o menu e resgatar.

O guia completo explica `FIRST`, `SECOND`, `THIRD`, `KILLER`, eventos em grupo, testes e erros comuns:

- [Usar com TitansBattle](docs/TITANSBATTLE.md)

Leia tambem:

- [Guia de instalacao](docs/INSTALACAO.md)
- [Comandos e permissoes](docs/COMANDOS.md)
- [Como personalizar o menu](docs/MENU.md)
- [Integracao com dragao](docs/INTEGRACAO-DRAGAO.md)
- [Usar com TitansBattle](docs/TITANSBATTLE.md)
- [Publicar no GitHub](docs/PUBLICAR-GITHUB.md)

## Build

No PowerShell:

```powershell
.\build.ps1
```

O jar sera gerado em:

```text
build/FocusCorreio.jar
```

## Licenca

Distribuido sob a licenca MIT. Veja [LICENSE](LICENSE).
