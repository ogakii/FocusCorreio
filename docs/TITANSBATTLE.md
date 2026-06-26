# Usar com TitansBattle

Este guia mostra como fazer o `TitansBattle` entregar premios no `/correio` usando o `FocusCorreio`.

A integracao nao precisa de API especial. Ela usa comandos de recompensa do proprio TitansBattle.

## Como Funciona

Pense em 3 partes:

| Parte | O que faz |
| --- | --- |
| `TitansBattle` | Controla o evento, descobre quem venceu e executa comandos de premio. |
| `FocusCorreio` | Recebe o comando e salva a recompensa no correio do jogador. |
| Jogador | Abre `/correio` e resgata o item quando quiser. |

O jogador nao envia nada para ninguem.

O comando de premio e executado pelo console/evento, nao pelo jogador.

## Permissoes Corretas

Para jogadores comuns, use somente:

```text
focuscorreio.usar
```

Com isso o jogador consegue:

- abrir `/correio`;
- mudar pagina pelo menu;
- atualizar o menu;
- resgatar recompensas.

Nao entregue para jogadores comuns:

```text
focuscorreio.admin
focuscorreio.*
```

Essas permissoes permitem enviar recompensas para outras pessoas e devem ficar apenas para staff, console e plugins de evento.

## Resumo Rapido

No arquivo do evento do TitansBattle, coloque um comando assim na recompensa:

```text
correio adicionar %player% DIAMOND 16 Premio do Evento
```

Quando o evento terminar:

1. O TitansBattle troca `%player%` pelo nome do vencedor.
2. O console executa o comando.
3. O FocusCorreio salva o item no correio desse jogador.
4. O jogador usa `/correio` para resgatar.

## Onde Fica o Arquivo do Evento

Crie ou edite um evento do TitansBattle.

Normalmente os eventos ficam em:

```text
plugins/TitansBattle/games/
```

Exemplo:

```text
plugins/TitansBattle/games/gladiador.yml
```

Dentro desse arquivo, procure a parte:

```yaml
prizes:
```

E edite o premio que voce quer.

## Entendendo FIRST, SECOND, THIRD e KILLER

O TitansBattle separa as recompensas por posicao.

| Chave | Quando usa |
| --- | --- |
| `FIRST` | Primeiro lugar / vencedor principal. |
| `SECOND` | Segundo lugar em torneios de eliminacao. |
| `THIRD` | Terceiro lugar em torneios de eliminacao. |
| `KILLER` | Jogador com mais abates, se essa opcao estiver ativada no evento. |

Em eventos Free For All, normalmente voce usa apenas `FIRST`.

Em eventos de eliminacao, voce pode usar `FIRST`, `SECOND` e `THIRD`.

## Premio Para Primeiro Lugar

Exemplo simples:

```yaml
prizes:
  FIRST:
    member.commands.enabled: true
    member.commands.command_list:
      - "correio adicionar %player% DIAMOND 16 Premio do Gladiador"
      - "correio adicionar %player% TRIPWIRE_HOOK 1 Chave do Gladiador"
```

O que isso faz:

- `member.commands.enabled: true` liga os comandos de premio.
- `member.commands.command_list` e a lista de comandos que o TitansBattle vai executar.
- `%player%` vira o nome do vencedor.
- Cada linha manda um item diferente para o correio.

## Primeiro, Segundo e Terceiro Lugar

```yaml
prizes:
  FIRST:
    member.commands.enabled: true
    member.commands.command_list:
      - "correio adicionar %player% NETHERITE_INGOT 3 Campeao do Torneio"
      - "correio adicionar %player% DIAMOND 32 Premio de Primeiro Lugar"

  SECOND:
    member.commands.enabled: true
    member.commands.command_list:
      - "correio adicionar %player% DIAMOND 16 Premio de Segundo Lugar"

  THIRD:
    member.commands.enabled: true
    member.commands.command_list:
      - "correio adicionar %player% EMERALD 16 Premio de Terceiro Lugar"
```

## Premio Para Quem Mais Matou

Se o seu evento estiver com killer ativado:

```yaml
killer: true
```

Voce pode configurar:

```yaml
prizes:
  KILLER:
    member.commands.enabled: true
    member.commands.command_list:
      - "correio adicionar %player% GOLDEN_APPLE 3 Mais Abates do Evento"
```

## Eventos em Grupo

Se o evento usa grupo, o TitansBattle pode separar premio de lider e premio de membro.

Para lider:

```yaml
prizes:
  FIRST:
    leader.commands.enabled: true
    leader.commands.command_list:
      - "correio adicionar %player% NETHERITE_INGOT 2 Lider Campeao"
```

Para membros:

```yaml
prizes:
  FIRST:
    member.commands.enabled: true
    member.commands.command_list:
      - "correio adicionar %player% DIAMOND 16 Membro Campeao"
```

Se quiser que o lider receba a mesma recompensa dos membros:

```yaml
prizes:
  FIRST:
    treat_leaders_as_members: true
    member.commands.enabled: true
    member.commands.command_list:
      - "correio adicionar %player% DIAMOND 16 Time Campeao"
```

## Varios Eventos, Varios Premios

Cada evento tem seu proprio arquivo. Entao cada um pode ter premios diferentes.

```text
gladiador.yml -> correio adicionar %player% DIAMOND 16 Premio do Gladiador
sumo.yml      -> correio adicionar %player% EMERALD 10 Premio do Sumo
arena.yml     -> correio adicionar %player% GOLD_INGOT 32 Premio da Arena
```

Assim voce cria quantos eventos quiser, cada um entregando um item especifico no correio.

## Como Recarregar Depois de Editar

Depois de alterar o arquivo do evento:

```text
/tb reload
```

Depois inicie o evento como voce ja faz normalmente.

Exemplo:

```text
/tb start gladiador
```

Se no seu servidor o comando de iniciar evento tiver outro nome, use o comando configurado no seu TitansBattle.

## Como Testar Antes do Evento

Antes de esperar o evento acabar, teste o comando direto no console:

```text
correio adicionar SeuNick DIAMOND 1 Teste TitansBattle
```

Depois entre no jogo e use:

```text
/correio
```

Se o item apareceu, o FocusCorreio esta certo. A partir dai, o que falta e o TitansBattle executar esse mesmo comando no fim do evento.

## O Que Nao Fazer

Nao coloque `/` no inicio do comando se o seu TitansBattle segue o padrao de comandos em lista:

```text
correio adicionar %player% DIAMOND 16 Premio
```

Evite:

```text
/correio adicionar %player% DIAMOND 16 Premio
```

Nao use recompensa em `items` se voce quer que va para o correio. Use `commands`.

Nao de `focuscorreio.admin` para jogadores.

## Problemas Comuns

### O premio nao chegou

Teste o comando no console:

```text
correio adicionar Steve DIAMOND 1 Teste TitansBattle
```

Se esse comando funcionar, o problema esta na configuracao do TitansBattle.

### O TitansBattle nao executou o comando

Confira se esta assim:

```yaml
member.commands.enabled: true
member.commands.command_list:
  - "correio adicionar %player% DIAMOND 16 Premio"
```

Se for premio de lider, confira `leader.commands.enabled`.

### O material deu invalido

Use nomes de materiais do Minecraft em ingles:

```text
DIAMOND
EMERALD
TRIPWIRE_HOOK
NETHERITE_INGOT
GOLDEN_APPLE
```

### O jogador recebeu permissao demais

No LuckPerms ou no plugin de permissao que voce usa, deixe jogador comum somente com:

```text
focuscorreio.usar
```

Remova:

```text
focuscorreio.admin
focuscorreio.*
```

## Fonte

- Wiki oficial do TitansBattle: https://github.com/RoinujNosde/TitansBattle/wiki
- Guia de criacao/edicao de evento: https://github.com/RoinujNosde/TitansBattle/wiki/Creating-and-editing-a-game
