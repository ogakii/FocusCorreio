# Guia de Instalacao

## Requisitos

- Java 17 ou superior.
- Servidor Bukkit, Spigot ou Paper.
- Permissao para colocar arquivos na pasta `plugins`.

## Instalando

1. Pare o servidor.
2. Coloque `FocusCorreio-2.0.0.jar` dentro da pasta `plugins`.
3. Inicie o servidor.
4. Confirme no console:

```text
[FocusCorreio] Enabling FocusCorreio v2.0.0
[FocusCorreio] FocusCorreio ativado.
```

5. Entre no servidor e use:

```text
/correio
```

## Atualizando

1. Pare o servidor.
2. Remova o jar antigo do correio.
3. Coloque o novo `FocusCorreio-2.0.0.jar`.
4. Inicie o servidor.

Os dados dos jogadores ficam em:

```text
plugins/FocusCorreio/data.yml
```

Faca backup dessa pasta antes de atualizar em servidor de producao.

## Permissoes

Para jogadores:

```text
focuscorreio.usar
```

Para staff:

```text
focuscorreio.admin
```
