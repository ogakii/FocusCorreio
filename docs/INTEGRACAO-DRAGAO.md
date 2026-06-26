# Integracao com Dragao

O FocusCorreio pode receber recompensas vindas do plugin de dragao integrado.

## Recomendado

Se voce usa o `EnderDragonSpawnerPlugin` integrado, deixe a recompensa interna do FocusCorreio desativada:

```yaml
automatic-rewards:
  ender-dragon:
    enabled: false
```

Depois, no plugin do dragao, configure:

```yaml
rewards:
  top1:
    delivery-mode: CORREIO
```

Tambem pode usar:

```yaml
rewards:
  top1:
    delivery-mode: AUTO
```

`AUTO` tenta enviar para o FocusCorreio e, se nao conseguir, pode entregar no inventario quando o fallback estiver ativado.

## Recompensa Propria do FocusCorreio

Se voce nao usa outro plugin de dragao, pode ativar a recompensa interna:

```text
/correio dragao definir DRAGON_EGG 1 Recompensa do Dragao
/correio dragao ativar
```

Para testar:

```text
/correio dragao dar Steve
```
