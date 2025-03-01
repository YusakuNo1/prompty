import json
import re
import typing
from pathlib import Path

import aiofiles
import yaml

_yaml_regex = re.compile(
    r"^\s*" + r"(?:---|\+\+\+)" + r"(.*?)" + r"(?:---|\+\+\+)" + r"\s*(.+)$",
    re.S | re.M,
)


def load_text(file_path, encoding="utf-8"):
    with open(file_path, encoding=encoding) as file:
        return file.read()


async def load_text_async(file_path, encoding="utf-8"):
    async with aiofiles.open(file_path, encoding=encoding) as f:
        content = await f.read()
        return content


def load_json(file_path, encoding="utf-8"):
    return json.loads(load_text(file_path, encoding=encoding))


async def load_json_async(file_path, encoding="utf-8"):
    # async file open
    content = await load_text_async(file_path, encoding=encoding)
    return json.loads(content)


def _find_global_config(prompty_path: Path = Path.cwd()) -> typing.Union[Path, None]:
    prompty_config = list(Path.cwd().glob("**/prompty.json"))

    if len(prompty_config) > 0:
        sorted_list = sorted(
            [
                c
                for c in prompty_config
                if len(c.parent.parts) <= len(prompty_path.parts)
            ],
            key=lambda p: len(p.parts),
        )
        return sorted_list[-1] if len(sorted_list) > 0 else None
    else:
        return None


def load_global_config(
    prompty_path: Path = Path.cwd(), configuration: str = "default"
) -> dict[str, typing.Any]:
    # prompty.config laying around?
    config = _find_global_config(prompty_path)

    # if there is one load it
    if config is not None:
        c = load_json(config)
        if configuration in c:
            return c[configuration]
        else:
            raise ValueError(f'Item "{configuration}" not found in "{config}"')

    return {}


async def load_global_config_async(
    prompty_path: Path = Path.cwd(), configuration: str = "default"
) -> dict[str, typing.Any]:
    # prompty.config laying around?
    config = _find_global_config(prompty_path)

    # if there is one load it
    if config is not None:
        c = await load_json_async(config)
        if configuration in c:
            return c[configuration]
        else:
            raise ValueError(f'Item "{configuration}" not found in "{config}"')

    return {}


def load_prompty(file_path, encoding="utf-8"):
    contents = load_text(file_path, encoding=encoding)
    return parse(contents)


async def load_prompty_async(file_path, encoding="utf-8"):
    contents = await load_text_async(file_path, encoding=encoding)
    return parse(contents)


def parse(contents):
    global _yaml_regex

    fmatter = ""
    body = ""
    result = _yaml_regex.search(contents)

    if result:
        fmatter = result.group(1)
        body = result.group(2)
    return {
        "attributes": yaml.load(fmatter, Loader=yaml.FullLoader),
        "body": body,
        "frontmatter": fmatter,
    }
