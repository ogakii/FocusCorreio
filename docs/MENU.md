# Menu do Correio

O menu aparece no jogo com o titulo:

```text
Correio
```

As recompensas ficam centralizadas automaticamente nas 5 primeiras linhas. A ultima linha e usada para botoes de controle.

## Layout

```text
Linha 1: recompensas
Linha 2: recompensas
Linha 3: recompensas
Linha 4: recompensas
Linha 5: recompensas
Linha 6: voltar | resgatar tudo | atualizar | proxima
```

Quando existe somente uma recompensa, ela fica no meio do menu. Quando existem poucas recompensas, elas continuam alinhadas de forma mais bonita.

## Configuracao Visual

Edite `plugins/FocusCorreio/config.yml`:

```yaml
gui:
  title: '&6Correio'
  empty-name: '&e&lNenhuma recompensa'
  claim-all: '&a&lResgatar tudo'
  refresh: '&b&lAtualizar'
  previous-page: '&ePagina anterior'
  next-page: '&eProxima pagina'
```

## Lore das Recompensas

```yaml
gui:
  reward-lore:
    - '&8Recompensa pendente'
    - ''
    - '&7Origem: &f%source%'
    - '&7Recebido em: &f%date%'
    - ''
    - '&eClique para resgatar.'
    - '&6Botao direito resgata tudo.'
```

Placeholders disponiveis:

| Placeholder | Significado |
| --- | --- |
| `%source%` | Origem da recompensa. |
| `%date%` | Data em que a recompensa foi criada. |
| `%page%` | Pagina atual. |
| `%pages%` | Total de paginas. |
